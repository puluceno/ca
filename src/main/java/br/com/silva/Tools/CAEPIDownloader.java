package br.com.silva.Tools;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.BrowserVersion;
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

public class CAEPIDownloader {

	public static void main(String[] args) {
		try {
			WebClient webClient = initializeClient();
			URL url = new URL("http://caepi.mte.gov.br/internet/ConsultaCAInternet.aspx");

			// TEST

			final LinkedList<WebWindow> windows = new LinkedList<WebWindow>();
			webClient.addWebWindowListener(new WebWindowListener() {
				@Override
				public void webWindowClosed(WebWindowEvent event) {
					System.out.println("a window is CLOSED: " + event.getOldPage());
				}

				@Override
				public void webWindowContentChanged(WebWindowEvent event) {
				}

				@Override
				public void webWindowOpened(WebWindowEvent event) {
					System.out.println("a NEW window opened: " + event.getNewPage());
					windows.add(event.getWebWindow());
				}
			});

			// end test

			HtmlPage page = (HtmlPage) webClient.getPage(url);

			HtmlTextInput number = (HtmlTextInput) page.getElementById("txtNumeroCA");
			number.setValueAttribute("25527");

			HtmlSubmitInput search = (HtmlSubmitInput) page.getElementById("btnConsultar");
			HtmlPage page2 = search.click();

			// WebResponse p2 = page2.getWebResponse();
			// InputStream is1 = p2.getContentAsStream();
			// String result1 = new BufferedReader(new
			// InputStreamReader(is1)).lines().collect(Collectors.joining("\n"));
			// System.out.println(result1);

			HtmlInput details = null;
			int tries = 7;
			while (tries > 0 && details == null) {
				tries--;
				details = (HtmlInput) page2.getElementById("PlaceHolderConteudo_grdListaResultado_btnDetalhar_0");
				synchronized (page2) {
					page2.wait(2000);
				}
				System.out.println("Remaining tries " + tries);
			}
			System.out.println(details);
			HtmlPage page3 = details.click();

			HtmlSubmitInput viewCA = null;

			int tries2 = 7;
			while (tries2 > 0 && viewCA == null) {
				tries2--;
				viewCA = (HtmlSubmitInput) page3.getElementById("PlaceHolderConteudo_btnVisualizarCA");
				synchronized (page3) {
					page3.wait(2000);
				}
				System.out.println("Remaining tries " + tries2);
			}
			System.out.println(viewCA);

			Page click = null;

			int tries3 = 7;
			WebWindow latestWindow = null;
			while (tries3 > 0 && click == null) {
				tries3--;
				click = viewCA.click();
				latestWindow = windows.getLast();
				synchronized (click) {
					click.wait(2000);
				}
				System.out.println("Remaining tries " + tries3);
			}

			WebResponse pdf = latestWindow.getEnclosedPage().getWebResponse();

			InputStream is = pdf.getContentAsStream();

			String result = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));

			System.out.println(result);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static WebClient initializeClient() {
		final WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.getOptions().setDoNotTrackEnabled(true);
		return webClient;
	}
}
