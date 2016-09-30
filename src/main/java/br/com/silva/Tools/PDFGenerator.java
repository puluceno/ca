package br.com.silva.Tools;

import java.util.List;
import java.util.Map;

import org.bson.Document;

import br.com.silva.business.FileImporter;
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
		gen.generatePDF();
	}

	@SuppressWarnings("unchecked")
	public void generatePDF() {
		try {
			String caFile = getClass().getClass().getResource(CA_FILE).getFile();
			// Compile jrxml file.
			JasperReport jasperReport = JasperCompileManager.compileReport(caFile);

			// Parameters for report
			Document document = FileImporter.findCADocument(new Document());
			Map<String, String> companyInfo = CNPJService.getCompanyInfo(document.getString("cnpj"));
			document.putAll(companyInfo);
			List<Document> reports = (List<Document>) document.get("reports");

			// DataSource
			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reports);

			JasperPrint print = JasperFillManager.fillReport(jasperReport, document, dataSource);

			// Export to PDF.
			JasperExportManager.exportReportToPdfFile(print, "ca.pdf");

			System.out.println("Done!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
