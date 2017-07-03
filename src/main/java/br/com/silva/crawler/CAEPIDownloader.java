package br.com.silva.crawler;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.bson.Document;
import org.pmw.tinylog.Logger;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import br.com.silva.business.CAReader;
import br.com.silva.data.CARepository;
import br.com.silva.exceptions.CAEPINotFoundException;
import br.com.silva.model.CA;
import br.com.silva.model.CAConstants;
import br.com.silva.model.JsonTransformer;
import br.com.silva.resources.MongoResource;
import br.com.silva.tools.MaskTools;
import br.com.silva.tools.TimeTools;

public class CAEPIDownloader extends Thread {

	private static MongoCollection<Document> caCollection = MongoResource.getDataBase("ca").getCollection("ca");
	private static MongoCollection<Document> caStatusCollection = MongoResource.getDataBase("ca")
			.getCollection("castatus");
	private static MongoCollection<Document> updateCollection = MongoResource.getDataBase("ca").getCollection("update");

	private static AtomicInteger number = new AtomicInteger(0);
	private static Object[] updateList;

	public static void main(String[] args) throws Exception {
		crawlCAS();
	}

	public static void crawlCAS() throws Exception {
		int threads = 1;
		Logger.info("Procedure started. Running with {} threads", threads);

		updateList = updateCollection.find().projection(fields(include("number", "processNumber"), excludeId()))
				.sort(ascending("number")).into(new ArrayList<Document>()).toArray();

		Logger.info("Crawling {} CA", updateList.length);

		List<CAEPIDownloader> list = new ArrayList<CAEPIDownloader>();

		for (int i = 0; i < threads; i++) {
			CAEPIDownloader thread = new CAEPIDownloader();
			list.add(thread);
		}
		for (CAEPIDownloader a : list) {
			Thread.sleep(1000);
			a.start();
		}

	}

	@Override
	public void run() {
		while (number.get() < updateList.length) {
			String caNumber = "";
			String processNumber = "";
			synchronized (updateList) {
				caNumber = ((Document) updateList[number.get()]).getString("number");
				processNumber = ((Document) updateList[number.getAndIncrement()]).getString("processNumber");
			}

			final LinkedList<WebWindow> windows = new LinkedList<WebWindow>();
			Logger.info("Downloading CA {}", caNumber);
			try (WebClient webClient = initializeClient()) {
				long beginCA = new Date().getTime();
				URL url = new URL("http://caepi.mte.gov.br/internet/ConsultaCAInternet.aspx");

				webClient.addWebWindowListener(new WebWindowListener() {
					@Override
					public void webWindowClosed(WebWindowEvent event) {
					}

					@Override
					public void webWindowContentChanged(WebWindowEvent event) {
					}

					@Override
					public void webWindowOpened(WebWindowEvent event) {
						windows.add(event.getWebWindow());
					}
				});

				HtmlPage page = (HtmlPage) webClient.getPage(url);

				HtmlTextInput inputNumber = (HtmlTextInput) page.getElementById("txtNumeroCA");
				inputNumber.setValueAttribute(caNumber);

				HtmlSubmitInput submitPage = (HtmlSubmitInput) page.getElementById("btnConsultar");
				page = submitPage.click();

				HtmlInput details = null;
				int tries = 40;
				String xpath = ".//td[contains(.,'" + processNumber + "')]/following-sibling::td[3]/input";
				while (tries > 0 && details == null) {
					tries--;
					details = (HtmlInput) page.getFirstByXPath(xpath);
					synchronized (page) {
						page.wait(1500);
					}
				}
				if (details == null)
					throw new CAEPINotFoundException("105");
				page = details.click();

				submitPage = null;

				int tries2 = 40;
				while (tries2 > 0 && submitPage == null) {
					tries2--;
					submitPage = (HtmlSubmitInput) page.getElementById("PlaceHolderConteudo_btnVisualizarCA");
					synchronized (page) {
						page.wait(1500);
					}
				}

				// ACQUIRE HISTORY
				HtmlTable table = (HtmlTable) page.getElementById("PlaceHolderConteudo_grdListaHistoricoAlteracao");
				List<Document> history = new ArrayList<Document>();
				if (table != null) {
					for (int i = 1; i < table.getRowCount(); i++) {
						for (final HtmlTableCell cell : table.getRow(i).getCells()) {
							if (cell.getIndex() == 1)
								history.add(new Document("date", cell.asText()));
							if (cell.getIndex() == 2)
								history.get(i - 1).append("status", cell.asText());
						}
					}
				}

				page = submitPage.click();
				int tries3 = 40;
				while (tries3 > 0 && windows.size() == 0) {
					tries3--;
					synchronized (page) {
						page.wait(1500);
					}
				}

				InputStream is = windows.getLast().getEnclosedPage().getWebResponse().getContentAsStream();

				readPDF(beginCA, caNumber, page, is);
				Logger.info("CA {} updated: {}", caNumber,
						caCollection.updateMany(eq("number", caNumber), set("history", history)).wasAcknowledged());

			} catch (Exception e) {
				if (e instanceof CAEPINotFoundException && e.getMessage().equals("105")) {
					caStatusCollection.updateOne(
							eq("number", caNumber), combine(set("number", caNumber), set("exist", false),
									set("downloaded", false), set("imported", false)),
							new UpdateOptions().upsert(true));
					Logger.info("CA {} updated to inexistent in the database", caNumber);
				} else {
					Logger.trace(e);
				}
			} finally {
				System.gc();
				// webClient.getCurrentWindow().getJobManager().removeAllJobs();
				// webClient.close();
			}
		}
		Logger.info("Operarion finished, there are no CA's left in the list");
	}

	private static synchronized void readPDF(long beginCA, String number, Page click, InputStream is)
			throws FileNotFoundException, IOException {
		File file = new File(CAConstants.CA_DIR + number + CAConstants.PDF_EXTENSION);

		OutputStream os = new FileOutputStream(file);
		byte[] bytes = new byte[1024];
		int read;
		try {
			while ((read = is.read(bytes)) != -1) {
				os.write(bytes, 0, read);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.trace(e);
		} finally {
			os.close();
			is.close();
			click.cleanUp();
			saveAndImportCA(beginCA, file);
		}
	}

	public static synchronized void saveAndImportCA(long beginCA, File file) {
		CA ca;
		try {
			ca = CAReader.readPDF(file.getAbsolutePath());
			String number = ca.getNumber();
			File newFileName = new File(CAConstants.CA_DIR + number
					+ "_" + (ca.getDate().contains("Condicionada")
							? MaskTools.unMaskProcessNumber(ca.getProcessNumber()) : MaskTools.unMaskDate(ca.getDate()))
					+ CAConstants.PDF_EXTENSION);
			boolean renamed = file.renameTo(newFileName);
			if (renamed) {
				if (CARepository.findCA(new Document("number", ca.getNumber()).append("date", ca.getDate())
						.append("processNumber", ca.getProcessNumber()), "number") == null) {
					caCollection.insertOne(JsonTransformer.toDocument(ca).append("fileName", newFileName.getName()));

					caStatusCollection
							.updateOne(
									eq("number", number), combine(set("number", number), set("exist", true),
											set("downloaded", true), set("imported", true)),
									new UpdateOptions().upsert(true));

					updateCollection.deleteOne(eq("number", number));

					Logger.info("CA {} found and saved under the file {}. Execution time: {}", number, newFileName,
							TimeTools.formatTime((int) ((new Date().getTime() - beginCA) / 1000)));
				} else {
					Logger.info("CA {} already present in the database.", ca.getNumber());

					caStatusCollection
							.updateOne(
									eq("number", number), combine(set("number", number), set("exist", true),
											set("downloaded", true), set("imported", true)),
									new UpdateOptions().upsert(true));

					updateCollection.deleteOne(eq("number", number));

				}

			} else {
				caStatusCollection.updateOne(eq("number", number), combine(set("number", number), set("exist", true),
						set("downloaded", true), set("imported", false)), new UpdateOptions().upsert(true));

				updateCollection.deleteOne(eq("number", number));

				Logger.info("Not imported! CA {} found and saved under the file {}. Execution time: {}", number,
						newFileName, TimeTools.formatTime((int) ((new Date().getTime() - beginCA) / 1000)));
			}

		} catch (Exception e) {
			Logger.trace(e);
		}
	}

	private static WebClient initializeClient() {
		final WebClient webClient = new WebClient();
		webClient.getOptions().setHistorySizeLimit(1);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.getOptions().setDoNotTrackEnabled(true);
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies")
				.setLevel(Level.OFF);

		return webClient;
	}
}
