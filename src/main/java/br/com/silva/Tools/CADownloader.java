package br.com.silva.Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;

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

		final WebClient webClient = new WebClient();
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.getOptions().setDoNotTrackEnabled(true);
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies")
				.setLevel(Level.OFF);

		int numberGenerated = 0;
		for (int number = 448; number < 100000; number++) {
			System.out.println("Downloading CA " + number);
			long beginCA = new Date().getTime();

			try {
				URL url = new URL("https://consultaca.com/" + number);

				HtmlPage page = (HtmlPage) webClient.getPage(url);

				HtmlAnchor anchor = (HtmlAnchor) page.getElementById("hlkSalvarCertificado");
				if (anchor != null) {
					Page click = anchor.click();
					WebResponse webResponse = click.getWebResponse();
					InputStream is = webResponse.getContentAsStream();
					File file = new File(DIR + number + PDF_EXTENSION);

					OutputStream os = new FileOutputStream(file);
					byte[] bytes = new byte[8 * 1024];
					int read;
					try {
						while ((read = is.read(bytes)) != 0) {
							os.write(bytes, 0, read);
						}
					} catch (Exception e) {
					} finally {
						os.close();
						is.close();
						numberGenerated++;
						Logger.info("CA {} encontrado e arquivado com o nome {}. Tempo de execução: {}", number,
								number + PDF_EXTENSION,
								TimeTools.formatTime((int) ((new Date().getTime() - beginCA) / 1000)));
					}
				} else
					Logger.info("CA {} não encontrado. Tempo de execução: {}", number,
							TimeTools.formatTime((int) ((new Date().getTime() - beginCA) / 1000)));

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				webClient.getCurrentWindow().getJobManager().removeAllJobs();
				webClient.close();
			}
		}
		long absoluteEnd = new Date().getTime();
		Logger.info("A operação total levou {}. {} arquivos foram gerados.",
				TimeTools.formatTime((int) (absoluteBegin - absoluteEnd) / 1000), numberGenerated);
	}

}
