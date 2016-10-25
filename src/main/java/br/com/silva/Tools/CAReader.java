package br.com.silva.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import br.com.silva.model.CA;
import br.com.silva.model.Report;

public class CAReader {

	public static void main(String[] args) throws IOException {
		PdfReader reader = new PdfReader("/home/pulu/Downloads/CA29440.pdf");
		CA ca = new CA();

		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			String page = PdfTextExtractor.getTextFromPage(reader, i);

			String[] line = page.split("\n");

			String caNumber = line[5].substring(33, 39);
			ca.setNumber(caNumber);

			String status = line[6];
			ca.setStatus(status);

			String date = line[8].substring(10, 20);
			ca.setDate(date);

			String processNumber = line[8].substring(38, line[8].length());
			ca.setProcessNumber(processNumber);

			String origin = line[9].substring(9, line[9].length());
			ca.setOrigin(origin);

			String equipment = page.substring(page.indexOf("Equipamento: ") + 13, page.indexOf("Descri��o: ") - 1);
			ca.setEquipment(equipment);

			String description = page.substring(page.indexOf("Descri��o: ") + 11, page.indexOf("Aprovado para:") - 1);
			ca.setDescription(description);

			String approvedFor = page.substring(page.indexOf("Aprovado para: ") + 15,
					page.indexOf("Restri��es/Limita��es: ") == -1 ? (page.indexOf("Observa��o: ") == -1
							? page.indexOf("Marca��o do CA: ") - 1 : page.indexOf("Observa��o: ") - 1)
							: page.indexOf("Restri��es/Limita��es: "));
			ca.setApprovedFor(approvedFor);

			String restrictions = "";
			if (page.contains("Restri��es/Limita��es: "))
				restrictions = page.substring(page.indexOf("Restri��es/Limita��es: ") + 23,
						page.contains("Observa��o: ") ? page.indexOf("Observa��o: ") - 1
								: page.indexOf("Marca��o do CA: ") - 1);
			ca.setRestrictions(restrictions);

			String observation = "";
			if (page.contains("Observa��o: "))
				observation = page.substring(page.indexOf("Observa��o: ") + 12, page.indexOf("Marca��o do CA: ") - 1);
			ca.setObservation(observation);

			String caLocation = page.substring(page.indexOf("Marca��o do CA: ") + 16,
					page.indexOf("Refer�ncias: ") - 1);
			ca.setCaLocation(caLocation);

			String references = page.substring(page.indexOf("Refer�ncias: ") + 13, (page.indexOf("Tamanhos: ") == -1
					? page.indexOf("Normas t�cnicas: ") - 1 : page.indexOf("Tamanhos: ") - 1));
			ca.setReferences(references);

			String size = "";
			if (page.contains("Tamanhos: "))
				size = page.substring(page.indexOf("Tamanhos: ") + 10, page.indexOf("Cores: ") == -1
						? (page.indexOf("Normas t�cnicas: ") - 1) : (page.indexOf("Cores: ") - 1));
			ca.setSize(size);

			String colors = "";
			if (page.contains("Cores: "))
				colors = page.substring(page.indexOf("Cores: ") + 7, page.indexOf("Normas t�cnicas: ") - 1);
			ca.setColors(colors);

			String techRules = page.substring(page.indexOf("Normas t�cnicas: ") + 17, page.indexOf("Laudos:") - 1);
			String[] techRulesArray = techRules.split(";");
			List<String> technicalRules = new ArrayList<String>(Arrays.asList(techRulesArray));
			ca.setTechnicalRules(technicalRules);

			List<Report> reports = new ArrayList<Report>();

			int nReports = StringUtils.countMatches(page, "N�. Laudo: ");
			for (int j = 1; j <= nReports; j++) {
				Report report = new Report();
				int numberIndex = StringUtils.ordinalIndexOf(page, "N�. Laudo: ", j);
				int labIndex = StringUtils.ordinalIndexOf(page, "Laborat�rio: ", j);

				String reportNumber = page.substring(numberIndex + 11, labIndex - 1);
				String laboratoryName = "";

				if (j + 1 <= nReports) {
					laboratoryName = page.substring(labIndex + 13,
							StringUtils.ordinalIndexOf(page, "N�. Laudo: ", j + 1) - 1);
				}
				if (j == nReports) {
					laboratoryName = page.substring(labIndex + 13, page.indexOf("Empresa: ") - 1);
				}

				report.setLaboratoryName(laboratoryName);
				report.setReportNumber(reportNumber);
				reports.add(report);
			}
			ca.setReports(reports);

			Map<String, List<String>> attenuationTable = new HashMap<String, List<String>>();
			if (page.contains("Tabela de Atenua��o")) {

				String[] freqs = page
						.substring(page.indexOf("Frequ�ncia (Hz): ") + 17, page.indexOf("Atenua��o db: ") - 1)
						.split(" ");
				LinkedList<String> frequencies = new LinkedList<String>(Arrays.asList(freqs));

				String[] dbs = page.substring(page.indexOf("Atenua��o db: ") + 14, page.indexOf("Desvio Padr�o: ") - 1)
						.split(" ");
				LinkedList<String> dbAttenuations = new LinkedList<String>(Arrays.asList(dbs));

				String[] devs = page.substring(page.indexOf("Desvio Padr�o: ") + 14, page.lastIndexOf("\n")).split(" ");
				LinkedList<String> deviations = new LinkedList<String>(Arrays.asList(devs));

				attenuationTable.put("frequencies", frequencies);
				attenuationTable.put("dbAttenuations", dbAttenuations);
				attenuationTable.put("deviations", deviations);
			}
			ca.setAttenuationTable(attenuationTable);
		}
	}

}
