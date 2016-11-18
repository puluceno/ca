package br.com.silva.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.pmw.tinylog.Logger;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import br.com.silva.model.CA;
import br.com.silva.model.Report;

public class CAReader {

	public static CA readPDF(String pathToPDF) {
		// public static void main(String[] args) {
		CA ca = new CA();

		try {
			// PdfReader reader = new PdfReader("/home/pulu/Downloads/13.pdf");
			PdfReader reader = new PdfReader(pathToPDF);
			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				String page = PdfTextExtractor.getTextFromPage(reader, i);

				String[] line = page.split("\n");

				int number = Integer.valueOf(
						line[5].substring(line[5].indexOf("Nº") + 3, line[5].length()).replaceAll("[^\\d]", ""));
				ca.setNumber(number);

				String date = "";
				String processNumber = "";
				if (page.contains("Validade: ")) {
					date = page.substring(page.indexOf("Validade: ") + 10, page.indexOf("Nº. do Processo: ") - 1);
					processNumber = page.substring(page.indexOf("Nº. do Processo: ") + 17,
							page.indexOf("Produto: ") - 1);
				}
				ca.setDate(date);
				ca.setProcessNumber(processNumber);

				String status = line[6].trim();
				ca.setStatus(status.isEmpty() ? date : status);

				String origin = "";
				if (page.contains("Produto: "))
					origin = page.substring(page.indexOf("Produto: ") + 9, page.indexOf("Equipamento: ") - 1);

				ca.setOrigin(origin);

				String equipment = page.substring(page.indexOf("Equipamento: ") + 13, page.indexOf("Descrição:") - 1);
				ca.setEquipment(removeNewLine(equipment));

				Map<String, List<String>> attenuationTable = new HashMap<String, List<String>>();
				if (page.contains("Tabela de Atenuação")) {

					String[] freqs = page
							.substring(page.indexOf("Frequência (Hz): ") + 17, page.indexOf("Atenuação db: ") - 1)
							.split(" ");
					LinkedList<String> frequencies = new LinkedList<String>(Arrays.asList(freqs));

					String[] dbs = page
							.substring(page.indexOf("Atenuação db: ") + 14, page.indexOf("Desvio Padrão: ") - 1)
							.split(" ");
					LinkedList<String> dbAttenuations = new LinkedList<String>(Arrays.asList(dbs));

					String[] devs = page.substring(page.indexOf("Desvio Padrão: ") + 15, page.lastIndexOf("\n"))
							.split(" ");
					LinkedList<String> deviations = new LinkedList<String>(Arrays.asList(devs));

					attenuationTable.put("frequencies", frequencies);
					attenuationTable.put("dbAttenuations", dbAttenuations);
					attenuationTable.put("deviations", deviations);
				}
				ca.setAttenuationTable(attenuationTable);

				String company = page.substring(page.indexOf("Empresa: ") + 9, page.indexOf("CNPJ:") - 1);
				ca.setCompany(removeNewLine(company));

				String cnpj = page.substring(page.indexOf("CNPJ: ") + 6, page.indexOf("CNPJ:") + 24);
				ca.setCnpj(removeNewLine(cnpj));

				if (!page.contains("Aprovado para:")) {
					ca.setApprovedFor("CA não contém este campo, portanto é irrelevante");
					return ca;
				}

				String description = "";
				if (page.contains("Descrição:"))
					description = page.substring(page.indexOf("Descrição:") + 11, page.indexOf("Aprovado para:") - 1);
				ca.setDescription(removeNewLine(description));

				String approvedFor = "";
				if (page.contains("Aprovado para: "))
					approvedFor = page.substring(page.indexOf("Aprovado para: ") + 15,
							page.indexOf("Restrições/Limitações: ") == -1
									? (page.indexOf("Observação: ") == -1 ? page.indexOf("Marcação do CA:") - 1
											: page.indexOf("Observação: ") - 1)
									: page.indexOf("Restrições/Limitações: "));
				ca.setApprovedFor(removeNewLine(approvedFor));

				String restrictions = "";
				if (page.contains("Restrições/Limitações: "))
					restrictions = page.substring(page.indexOf("Restrições/Limitações: ") + 23,
							page.contains("Observação: ") ? page.indexOf("Observação: ") - 1
									: page.indexOf("Marcação do CA: ") - 1);
				ca.setRestrictions(removeNewLine(restrictions));

				String observation = "";
				if (page.contains("Observação: "))
					observation = page.substring(page.indexOf("Observação: ") + 12,
							page.indexOf("Marcação do CA: ") - 1);
				ca.setObservation(removeNewLine(observation));

				if (page.contains("Marcação do CA:")) {
					String caLocation = page.substring(page.indexOf("Marcação do CA: ") + 16,
							page.indexOf("Referências: ") - 1);
					ca.setCaLocation(removeNewLine(caLocation));
				}

				boolean hasTechnician = page.contains("Responsável Técnico:");
				String references = "";

				if (page.contains("Referências:")) {
					if (hasTechnician)
						references = page.substring(page.indexOf("Referências:") + 13,
								page.indexOf("Responsável Técnico:") - 1);
					else
						references = page.substring(page.indexOf("Referências:") + 13, (page.indexOf("Tamanhos:") == -1
								? page.indexOf("Normas técnicas: ") - 1 : page.indexOf("Tamanhos: ") - 1));
					ca.setReferences(removeNewLine(references));
				}

				String size = "";
				if (page.contains("Tamanhos: ")) {
					if (hasTechnician)
						size = page.substring(page.indexOf("Tamanhos: ") + 10, page.indexOf("Cores: ") == -1
								? (page.indexOf("Responsável Técnico:") - 1) : page.indexOf("Cores") - 1);
					else
						size = (page.substring(page.indexOf("Tamanhos: ") + 10, page.indexOf("Cores: ") == -1
								? (page.indexOf("Normas técnicas: ") - 1) : (page.indexOf("Cores: ") - 1)));

					ca.setSize(removeNewLine(size));
				}

				String colors = "";
				if (page.contains("Cores: "))
					colors = page.substring(page.indexOf("Cores: ") + 7,
							page.indexOf("Marcação do selo do Inmetro: ") == -1
									? (page.indexOf("Normas técnicas: ") - 1)
									: (page.indexOf("Marcação do selo do Inmetro: ") - 1));
				ca.setColors(removeNewLine(colors));

				String technician = "";
				if (hasTechnician)
					technician = page.substring(page.indexOf("Responsável Técnico:") + 21,
							page.indexOf("Nº Registro Profissional:") - 1);
				ca.setTechnician(removeNewLine(technician));

				String professionalRegistration = "";
				if (page.contains("Nº Registro Profissional:"))
					professionalRegistration = page.substring(page.indexOf("Nº Registro Profissional: ") + 26,
							page.indexOf("Empresa:") - 1);
				ca.setProfessionalRegistration(removeNewLine(professionalRegistration));

				String inmetroSticker = "";
				if (page.contains("Marcação do selo do Inmetro: "))
					inmetroSticker = page.substring(page.indexOf("Marcação do selo do Inmetro: ") + 29,
							page.indexOf("Atestado de conformidade Inmetro:") - 1);
				ca.setInmetroSticker(removeNewLine(inmetroSticker));

				String inmetroConformityProof = "";
				if (page.contains("Atestado de conformidade Inmetro: "))
					inmetroConformityProof = page.substring(page.indexOf("Atestado de conformidade Inmetro: ") + 34,
							page.indexOf("Normas técnicas: ") - 1);
				ca.setInmetroConformityProof(removeNewLine(inmetroConformityProof));

				String techRules = "";
				if (page.contains("Normas técnicas: ")) {
					techRules = page.substring(page.indexOf("Normas técnicas: ") + 17,
							page.indexOf("Laudos:") == -1 ? page.indexOf("Empresa: ") - 1 : page.indexOf("Laudos:"));
				}
				String[] techRulesArray = removeNewLine(techRules).split(";");
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

					report.setLaboratoryName(removeNewLine(laboratoryName));
					report.setReportNumber(reportNumber);
					reports.add(report);
				}
				ca.setReports(reports);

			}
		} catch (Exception e) {
			Logger.trace(e);
		}
		return ca;
	}

	private static String removeNewLine(String string) {
		return string.replace("\n", " ").replace("\r", " ").trim();
	}
}
