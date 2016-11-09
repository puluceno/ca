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

	public static CA readPDF(String pathToPDF) throws IOException {
		// public static void main(String[] args) throws IOException {
		// PdfReader reader = new PdfReader("/home/pulu/Documents/CAs/445.pdf");
		PdfReader reader = new PdfReader(pathToPDF);
		CA ca = new CA();

		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			String page = PdfTextExtractor.getTextFromPage(reader, i);

			String[] line = page.split("\n");

			String caNumber = "";
			if (line[5].length() == 39)
				caNumber = line[5].substring(33, 39);
			else if (line[5].length() == 38) {
				caNumber = line[5].substring(33, 38);
			} else if (line[5].length() == 37) {
				caNumber = line[5].substring(33, 37);
			}
			ca.setNumber(caNumber.trim());

			String status = line[6];
			ca.setStatus(status);

			String date = "";
			String processNumber = "";
			if (!line[8].contains("Condicionada")) {
				date = line[8].substring(10, 20);
				processNumber = line[8].substring(38, line[8].length());
			} else {
				date = line[8].substring(10, 69);
				processNumber = line[8].substring(86, line[8].length());
			}
			ca.setDate(date);
			ca.setProcessNumber(processNumber);

			String origin = line[9].substring(9, line[9].length());
			ca.setOrigin(origin);

			String equipment = page.substring(page.indexOf("Equipamento: ") + 13, page.indexOf("Descrição:") - 1);
			ca.setEquipment(equipment);

			Map<String, List<String>> attenuationTable = new HashMap<String, List<String>>();
			if (page.contains("Tabela de Atenuação")) {

				String[] freqs = page
						.substring(page.indexOf("Frequência (Hz): ") + 17, page.indexOf("Atenuação db: ") - 1)
						.split(" ");
				LinkedList<String> frequencies = new LinkedList<String>(Arrays.asList(freqs));

				String[] dbs = page.substring(page.indexOf("Atenuação db: ") + 14, page.indexOf("Desvio Padrão: ") - 1)
						.split(" ");
				LinkedList<String> dbAttenuations = new LinkedList<String>(Arrays.asList(dbs));

				String[] devs = page.substring(page.indexOf("Desvio Padrão: ") + 15, page.lastIndexOf("\n")).split(" ");
				LinkedList<String> deviations = new LinkedList<String>(Arrays.asList(devs));

				attenuationTable.put("frequencies", frequencies);
				attenuationTable.put("dbAttenuations", dbAttenuations);
				attenuationTable.put("deviations", deviations);
			}
			ca.setAttenuationTable(attenuationTable);

			String company = page.substring(page.indexOf("Empresa: ") + 9, page.indexOf("CNPJ:") - 1);
			ca.setCompany(company);

			String cnpj = page.substring(page.indexOf("CNPJ: ") + 6, page.indexOf("CNPJ:") + 24);
			ca.setCnpj(cnpj.trim());

			if (!page.contains("Aprovado para:")) {
				return ca;
			}

			String description = page.substring(page.indexOf("Descrição:") + 11, page.indexOf("Aprovado para:") - 1);
			ca.setDescription(description);

			String approvedFor = page.substring(page.indexOf("Aprovado para: ") + 15,
					page.indexOf("Restrições/Limitações: ") == -1 ? (page.indexOf("Observação: ") == -1
							? page.indexOf("Marcação do CA: ") - 1 : page.indexOf("Observação: ") - 1)
							: page.indexOf("Restrições/Limitações: "));
			ca.setApprovedFor(approvedFor);

			String restrictions = "";
			if (page.contains("Restrições/Limitações: "))
				restrictions = page.substring(page.indexOf("Restrições/Limitações: ") + 23,
						page.contains("Observação: ") ? page.indexOf("Observação: ") - 1
								: page.indexOf("Marcação do CA: ") - 1);
			ca.setRestrictions(restrictions);

			String observation = "";
			if (page.contains("Observação: "))
				observation = page.substring(page.indexOf("Observação: ") + 12, page.indexOf("Marcação do CA: ") - 1);
			ca.setObservation(observation);

			String caLocation = page.substring(page.indexOf("Marcação do CA: ") + 16,
					page.indexOf("Referências: ") - 1);
			ca.setCaLocation(caLocation);

			String references = page.substring(page.indexOf("Referências: ") + 13, (page.indexOf("Tamanhos: ") == -1
					? page.indexOf("Normas técnicas: ") - 1 : page.indexOf("Tamanhos: ") - 1));
			ca.setReferences(references);

			String size = "";
			if (page.contains("Tamanhos: "))
				size = page.substring(page.indexOf("Tamanhos: ") + 10, page.indexOf("Cores: ") == -1
						? (page.indexOf("Normas técnicas: ") - 1) : (page.indexOf("Cores: ") - 1));
			ca.setSize(size);

			String colors = "";
			if (page.contains("Cores: "))
				colors = page.substring(page.indexOf("Cores: ") + 7,
						page.indexOf("Marcação do selo do Inmetro: ") == -1 ? (page.indexOf("Normas técnicas: ") - 1)
								: (page.indexOf("Marcação do selo do Inmetro: ") - 1));
			ca.setColors(colors);

			String inmetroSticker = "";
			if (page.contains("Marcação do selo do Inmetro: "))
				inmetroSticker = page.substring(page.indexOf("Marcação do selo do Inmetro: ") + 29,
						page.indexOf("Atestado de conformidade Inmetro:") - 1);
			ca.setInmetroSticker(inmetroSticker);

			String inmetroConformityProof = "";
			if (page.contains("Atestado de conformidade Inmetro: "))
				inmetroConformityProof = page.substring(page.indexOf("Atestado de conformidade Inmetro: ") + 34,
						page.indexOf("Normas técnicas: ") - 1);
			ca.setInmetroConformityProof(inmetroConformityProof);

			String techRules = page.substring(page.indexOf("Normas técnicas: ") + 17,
					page.indexOf("Laudos:") == -1 ? page.indexOf("Empresa: ") - 1 : page.indexOf("Laudos:"));
			String[] techRulesArray = techRules.split(";");
			List<String> technicalRules = new ArrayList<String>(Arrays.asList(techRulesArray));
			ca.setTechnicalRules(technicalRules);

			List<Report> reports = new ArrayList<Report>();

			int nReports = StringUtils.countMatches(page, "Nº. Laudo: ");
			for (int j = 1; j <= nReports; j++) {
				Report report = new Report();
				int numberIndex = StringUtils.ordinalIndexOf(page, "Nº. Laudo: ", j);
				int labIndex = StringUtils.ordinalIndexOf(page, "Laboratório: ", j);

				String reportNumber = page.substring(numberIndex + 11, labIndex - 1);
				String laboratoryName = "";

				if (j + 1 <= nReports) {
					laboratoryName = page.substring(labIndex + 13,
							StringUtils.ordinalIndexOf(page, "Nº. Laudo: ", j + 1) - 1);
				}
				if (j == nReports) {
					laboratoryName = page.substring(labIndex + 13, page.indexOf("Empresa: ") - 1);
				}

				report.setLaboratoryName(laboratoryName);
				report.setReportNumber(reportNumber);
				reports.add(report);
			}
			ca.setReports(reports);

		}
		return ca;
		// System.out.println(ca);
	}

}
