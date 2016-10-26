package br.com.silva.Tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class CADownloader {

	private static final String PDF_EXTENSION = ".pdf";
	private static final String DIR = System.getProperty("user.home") + File.separator + "Documents" + File.separator
			+ "CAs/";

	public static void main(String[] args) {
		Logger.info("Procedure started!");
		long absoluteBegin = new Date().getTime();

		File directory = new File(DIR);
		if (!directory.exists())
			directory.mkdirs();

		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies")
				.setLevel(Level.OFF);

		int numberGenerated = 0;

		for (int number = 3305; number < 100000; number++) {
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
						Logger.info("{} - CA {} n�o encontrado na origem.", number, number);
						logDefault();
					} else {
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
						} finally {
							os.close();
							is.close();
							webResponse.cleanUp();
							numberGenerated++;
							Logger.info("CA {} encontrado e arquivado com o nome {}. Tempo de execu��o: {}", number,
									number + PDF_EXTENSION,
									TimeTools.formatTime((int) ((new Date().getTime() - beginCA) / 1000)));
						}
					}
				} else
					Logger.info("CA {} inexistente. Tempo de execu��o: {}", number,
							TimeTools.formatTime((int) ((new Date().getTime() - beginCA) / 1000)));

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				webClient.getCurrentWindow().getJobManager().removeAllJobs();
				webClient.close();
				System.gc();
			}
		}
		long absoluteEnd = new Date().getTime();
		Logger.info("A opera��o total levou {}. {} arquivos foram gerados.",
				TimeTools.formatTime((int) (absoluteBegin - absoluteEnd) / 1000), numberGenerated);
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
