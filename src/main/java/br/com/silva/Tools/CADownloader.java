package br.com.silva.Tools;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bson.Document;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import br.com.silva.model.CA;
import br.com.silva.resources.MongoResource;

public class CADownloader {

	private static final String PDF_EXTENSION = ".pdf";
	private static final String DIR = System.getProperty("user.home") + File.separator + "Documents" + File.separator
			+ "CAs/";
	private static MongoCollection<Document> caPdfCollection = MongoResource.getDataBase("ca").getCollection("capdf");
	private static MongoCollection<Document> caStatusCollection = MongoResource.getDataBase("ca")
			.getCollection("castatus");

	public static void main(String[] args) {
		init();
		long absoluteBegin = new Date().getTime();
		int numberGenerated = 0;

		List<Document> list = caStatusCollection.find().projection(fields(include("caNumber"), excludeId()))
				.sort(descending("caNumber")).limit(1).into(new ArrayList<Document>());
		int number = 1;
		if (list.size() > 0)
			number += list.get(0).getInteger("caNumber", 1);

		while (number < 100000) {
			System.out.println("Downloading CA " + number);
			WebClient webClient = initializeClient();
			long beginCA = new Date().getTime();

			try {
				URL url = new URL("https://consultaca.com/" + number);

				HtmlPage page = (HtmlPage) webClient.getPage(url);

				HtmlAnchor anchor = (HtmlAnchor) page.getElementById("hlkSalvarCertificado");
				if (anchor != null) {
					Page click = anchor.click();
					WebResponse webResponse = click.getWebResponse();
					InputStream is = webResponse.getContentAsStream();

					String result = new BufferedReader(new InputStreamReader(is)).lines()
							.collect(Collectors.joining("\n"));
					if (result.contains("informe o seu e-mail no campo abaixo que enviamos")) {
						logNotFound();
						Logger.info("{} - CA {} não encontrado na origem.", number, number);
						logDefault();

						// Add status to the database
						caStatusCollection.insertOne(new Document().append("caNumber", number).append("exist", true)
								.append("downloaded", false).append("imported", false));
					} else {
						File file = new File(DIR + number + PDF_EXTENSION);

						OutputStream os = new FileOutputStream(file);
						byte[] bytes = new byte[1024];
						int read;
						try {
							is.reset();
							while ((read = is.read(bytes)) != -1) {
								os.write(bytes, 0, read);
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							os.close();
							is.close();
							webResponse.cleanUp();
							numberGenerated++;
							Object[] fromPDF = extractDateFromPDF(file);
							File newFileName = new File(DIR + number + fromPDF[1] + PDF_EXTENSION);
							boolean renamed = file.renameTo(newFileName);
							if (renamed) {
								caPdfCollection.insertOne(CAParser.toDocument((CA) fromPDF[0]).append("fileName",
										newFileName.getAbsolutePath()));

								caStatusCollection.updateOne(
										eq("caNumber", number), combine(set("caNumber", number), set("exist", true),
												set("downloaded", true), set("imported", true)),
										new UpdateOptions().upsert(true));
							}

							Logger.info("CA {} encontrado e arquivado com o nome {}. Tempo de execução: {}", number,
									newFileName, TimeTools.formatTime((int) ((new Date().getTime() - beginCA) / 1000)));
						}
					}
				} else {
					Logger.info("CA {} inexistente. Tempo de execução: {}", number,
							TimeTools.formatTime((int) ((new Date().getTime() - beginCA) / 1000)));
					caStatusCollection.insertOne(new Document().append("caNumber", number).append("exist", false));
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				webClient.getCurrentWindow().getJobManager().removeAllJobs();
				webClient.close();
				System.gc();
			}
			number++;
		}
		long absoluteEnd = new Date().getTime();
		Logger.info("A operação total levou {}. {} arquivos foram gerados.",
				TimeTools.formatTime((int) (absoluteBegin - absoluteEnd) / 1000), numberGenerated);
	}

	private static void init() {
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies")
				.setLevel(Level.OFF);
		Logger.info("Procedure started!");
		File directory = new File(DIR);
		if (!directory.exists())
			directory.mkdirs();

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
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (date.contains("Condicionada"))
			objs[1] = "_" + MaskTools.unmasProcessNumber(processNumber);
		else
			objs[1] = "_" + date.replaceAll("/", "");
		return objs;
	}

	private static void logNotFound() {
		try {
			Configurator.fromResource("tinylogNotFound.properties").activate();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void logDefault() {
		try {
			Configurator.fromResource("tinylog.properties").activate();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static WebClient initializeClient() {
		final WebClient webClient = new WebClient();
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.getOptions().setDoNotTrackEnabled(true);
		return webClient;
	}

}
