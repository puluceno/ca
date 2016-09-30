package br.com.silva.business;

import java.util.List;

import org.bson.Document;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class TestPDF {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		// Compile jrxml file.
		JasperReport jasperReport = JasperCompileManager
				.compileReport("C:/Users/tpulu/JaspersoftWorkspace/MyReports/ca2.jrxml");
		

		// Parameters for report
		Document document = FileImporter.findCADocument(new Document());
		List<Document> reports = (List<Document>) document.get("reports");

		// DataSource
		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reports);

		JasperPrint print = JasperFillManager.fillReport(jasperReport, document, dataSource);

		// Export to PDF.
		JasperExportManager.exportReportToPdfFile(print, "ca.pdf");

		System.out.println("Done!");
	}
}
