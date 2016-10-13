package br.com.silva.Tools;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.pmw.tinylog.Logger;

import com.mashape.unirest.http.exceptions.UnirestException;

import br.com.silva.service.CNPJService;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import spark.Response;

public class PDFGenerator {

	private final static String CA_FILE = "/reports/ca.jasper";

	public static HttpServletResponse getPDF(Response res, Document ca) throws Exception {
		PDFGenerator gen = new PDFGenerator();
		gen.generatePDF(ca);

		String fileName = ca.getString("number") + ".pdf";

		try {
			byte[] bytes = Files.readAllBytes(Paths.get(fileName));
			HttpServletResponse raw = res.raw();

			raw.setContentType("application/pdf");
			raw.addHeader("Content-Disposition", "attachment; filename=" + ca.getString("number") + ".pdf");
			raw.getOutputStream().write(bytes);
			raw.getOutputStream().flush();
			raw.getOutputStream().close();

			Logger.info("PDF file {} generated and sent to the user.", fileName);
			return raw;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.trace(e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void generatePDF(Document ca) throws Exception {
		try {
			long begin = new Date().getTime();

			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(getClass().getResourceAsStream(CA_FILE));

			// Parameters for report
			Map<String, String> companyInfo = CNPJService.getCompanyInfo(ca.getString("cnpj"));
			ca.putAll(companyInfo);
			List<Document> reports = (List<Document>) ca.get("reports");

			// DataSource
			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reports);

			JasperPrint print = JasperFillManager.fillReport(jasperReport, ca, dataSource);

			// Export to PDF.
			String fileName = ca.getString("number") + ".pdf";
			JasperExportManager.exportReportToPdfFile(print, fileName);

			Logger.info("PDF file  {} generated in {}", fileName, (new Date().getTime() - begin) + "ms.");

		} catch (Exception e) {
			if (e instanceof NullPointerException)
				Logger.error("CA with number {} does not exist.", ca.get("number"));
			else if (e instanceof UnirestException) {
				Logger.error("Error when contacting external service for CNPJ.");
				Logger.trace(e);
				throw new Exception("É possível apenas gerar um CA a cada 60 segundos.");
			} else
				Logger.trace(e);
		}
	}

	public static boolean clearDocument(String fileName) {
		File file = new File(fileName);
		return file.delete();
	}
}
