package br.com.silva.Tools;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.pmw.tinylog.Logger;

import br.com.silva.service.CAService;
import br.com.silva.service.CNPJService;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class PDFGenerator {

	private final static String CA_FILE = "/reports/ca.jrxml";

	public static void main(String[] args) {
		PDFGenerator gen = new PDFGenerator();
		gen.generatePDF("26294");
	}

	@SuppressWarnings("unchecked")
	public void generatePDF(String number) {
		try {
			long begin = new Date().getTime();
			String caFile = getClass().getClass().getResource(CA_FILE).getFile();

			// Compile jrxml file.
			JasperReport jasperReport = JasperCompileManager.compileReport(caFile);

			// Parameters for report
			Document document = CAService.findCA(new Document("number", number));
			Map<String, String> companyInfo = CNPJService.getCompanyInfo(document.getString("cnpj"));
			document.putAll(companyInfo);
			List<Document> reports = (List<Document>) document.get("reports");

			// DataSource
			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reports);

			JasperPrint print = JasperFillManager.fillReport(jasperReport, document, dataSource);

			// Export to PDF.
			JasperExportManager.exportReportToPdfFile(print, "ca.pdf");

			Logger.info("PDF file generated in {}", (new Date().getTime() - begin) + "ms.");
		} catch (Exception e) {
			if (e.getClass().equals(NullPointerException.class))
				Logger.error("CA with number {} does not exist.", number);
			else
				Logger.trace(e);
		}
	}
}
