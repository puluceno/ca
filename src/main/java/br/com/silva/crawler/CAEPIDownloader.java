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
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.FileWriter;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import br.com.silva.business.CAReader;
import br.com.silva.exceptions.CAEPINotFoundException;
import br.com.silva.model.CA;
import br.com.silva.model.CAParser;
import br.com.silva.resources.MongoResource;
import br.com.silva.tools.MaskTools;
import br.com.silva.tools.TimeTools;

public class CAEPIDownloader extends Thread {

	private static MongoCollection<Document> caCollection = MongoResource.getDataBase("ca").getCollection("ca");
	private static MongoCollection<Document> caStatusCollection = MongoResource.getDataBase("ca")
			.getCollection("castatus");
	private static MongoCollection<Document> updateCollection = MongoResource.getDataBase("ca").getCollection("update");

	private static final String PDF_EXTENSION = ".pdf";
	// private static final String DIR = System.getProperty("user.home") +
	// File.separator + "CAs/";
	private static final String DIR = "C:" + File.separator + "xampp" + File.separator + "htdocs" + File.separator
			+ "CAs" + File.separator;

	private static AtomicInteger number = new AtomicInteger(0);
	private static Object[] updateList;

	public static void main(String[] args) throws Exception {
		crawlCAS();
	}

	public static void crawlCAS() throws Exception {
		Logger.info("Procedure started");
		int threads = 8;
		Logger.info("Running with {} threads", threads);

		init();

		updateList = updateCollection.find().projection(fields(include("number"), excludeId()))
				.sort(ascending("number")).into(new ArrayList<Document>()).toArray();

		List<CAEPIDownloader> list = new ArrayList<CAEPIDownloader>();

		for (int i = 0; i < threads; i++) {
			CAEPIDownloader thread = new CAEPIDownloader();
			list.add(thread);
		}
		for (CAEPIDownloader a : list) {
			Thread.sleep(1000);
			a.start();
			Logger.info("Procedure started");
		}

	}

	@Override
	public void run() {
		while (number.get() < updateList.length) {
			String caNumber = ((Document) updateList[number.getAndIncrement()]).getString("number");

			WebClient webClient = initializeClient();
			System.out.println("Downloading CA " + caNumber);

			try {
				long beginCA = new Date().getTime();
				URL url = new URL("http://caepi.mte.gov.br/internet/ConsultaCAInternet.aspx");

				final LinkedList<WebWindow> windows = new LinkedList<WebWindow>();
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

				HtmlSubmitInput search = (HtmlSubmitInput) page.getElementById("btnConsultar");
				HtmlPage page2 = search.click();

				HtmlInput details = null;
				int tries = 30;
				while (tries > 0 && details == null) {
					tries--;
					details = (HtmlInput) page2.getElementById("PlaceHolderConteudo_grdListaResultado_btnDetalhar_0");
					synchronized (page2) {
						page2.wait(1500);
					}
				}
				if (details == null)
					throw new CAEPINotFoundException("105");
				HtmlPage page3 = details.click();

				HtmlSubmitInput viewCA = null;

				int tries2 = 30;
				while (tries2 > 0 && viewCA == null) {
					tries2--;
					viewCA = (HtmlSubmitInput) page3.getElementById("PlaceHolderConteudo_btnVisualizarCA");
					synchronized (page3) {
						page3.wait(1500);
					}
				}

				Page click = viewCA.click();
				int tries3 = 30;
				while (tries3 > 0 && windows.size() == 0) {
					tries3--;
					synchronized (click) {
						click.wait(1500);
					}
				}
				WebWindow latestWindow = windows.getLast();
				WebResponse pdf = latestWindow.getEnclosedPage().getWebResponse();
				InputStream is = pdf.getContentAsStream();
				pdf.cleanUp();

				saveAndImportCA(beginCA, caNumber, click, is);

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
				webClient.getCurrentWindow().getJobManager().removeAllJobs();
				webClient.close();
			}
		}
		Logger.info("Operarion finished, there are no CA's left in the list");
	}

	private static synchronized void saveAndImportCA(long beginCA, String number, Page click, InputStream is)
			throws FileNotFoundException, IOException {
		File file = new File(DIR + number + PDF_EXTENSION);

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
			Object[] fromPDF = extractDateFromPDF(file);
			File newFileName = new File(DIR + number + fromPDF[1] + PDF_EXTENSION);
			boolean renamed = file.renameTo(newFileName);
			if (renamed) {
				caCollection.insertOne(
						CAParser.toDocument((CA) fromPDF[0]).append("fileName", newFileName.getAbsolutePath()));

				caStatusCollection.updateOne(eq("number", number), combine(set("number", number), set("exist", true),
						set("downloaded", true), set("imported", true)), new UpdateOptions().upsert(true));

				updateCollection.deleteOne(eq("number", number));

				Logger.info("CA {} encontrado e arquivado com o nome {}. Tempo de execu��o: {}", number, newFileName,
						TimeTools.formatTime((int) ((new Date().getTime() - beginCA) / 1000)));

			} else {
				caStatusCollection.updateOne(eq("number", number), combine(set("number", number), set("exist", true),
						set("downloaded", true), set("imported", false)), new UpdateOptions().upsert(true));

				Logger.info("N�o importado! CA {} encontrado e arquivado com o nome {}. Tempo de execu��o: {}", number,
						newFileName, TimeTools.formatTime((int) ((new Date().getTime() - beginCA) / 1000)));
			}
		}
	}

	private static Object[] extractDateFromPDF(File file) {
		Object[] objs = new Object[2];
		String date = "";
		String processNumber = "";
		try {
			CA ca = CAReader.readPDF(file.getAbsolutePath());
			objs[0] = ca;
			date = ca.getDate();
			processNumber = ca.getProcessNumber();
		} catch (Exception e) {
			Logger.trace(e);
		}

		if (date.contains("Condicionada"))
			objs[1] = "_" + MaskTools.unmasProcessNumber(processNumber);
		else
			objs[1] = "_" + date.replaceAll("/", "");
		return objs;
	}

	private static void init() {
		Configurator.defaultConfig()
				.writer(new FileWriter(System.getProperty("user.home") + File.separator + "log.txt")).activate();

		File directory = new File(DIR);
		if (!directory.exists())
			directory.mkdirs();
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
