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
						line[5].substring(line[5].indexOf("N�") + 3, line[5].length()).replaceAll("[^\\d]", ""));
				ca.setNumber(number);

				String date = "";
				String processNumber = "";
				if (page.contains("Validade: ")) {
					date = page.substring(page.indexOf("Validade: ") + 10, page.indexOf("N�. do Processo: ") - 1);
					processNumber = page.substring(page.indexOf("N�. do Processo: ") + 17,
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

				String equipment = page.substring(page.indexOf("Equipamento: ") + 13, page.indexOf("Descri��o:") - 1);
				ca.setEquipment(removeNewLine(equipment));

				Map<String, List<String>> attenuationTable = new HashMap<String, List<String>>();
				if (page.contains("Tabela de Atenua��o")) {

					String[] freqs = page
							.substring(page.indexOf("Frequ�ncia (Hz): ") + 17, page.indexOf("Atenua��o db: ") - 1)
							.split(" ");
					LinkedList<String> frequencies = new LinkedList<String>(Arrays.asList(freqs));

					String[] dbs = page
							.substring(page.indexOf("Atenua��o db: ") + 14, page.indexOf("Desvio Padr�o: ") - 1)
							.split(" ");
					LinkedList<String> dbAttenuations = new LinkedList<String>(Arrays.asList(dbs));

					String[] devs = page.substring(page.indexOf("Desvio Padr�o: ") + 15, page.lastIndexOf("\n"))
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
					ca.setApprovedFor("CA n�o cont�m este campo, portanto � irrelevante");
					return ca;
				}

				String description = "";
				if (page.contains("Descri��o:"))
					description = page.substring(page.indexOf("Descri��o:") + 11, page.indexOf("Aprovado para:") - 1);
				ca.setDescription(removeNewLine(description));

				String approvedFor = "";
				if (page.contains("Aprovado para: "))
					approvedFor = page.substring(page.indexOf("Aprovado para: ") + 15,
							page.indexOf("Restri��es/Limita��es: ") == -1
									? (page.indexOf("Observa��o: ") == -1 ? page.indexOf("Marca��o do CA:") - 1
											: page.indexOf("Observa��o: ") - 1)
									: page.indexOf("Restri��es/Limita��es: "));
				ca.setApprovedFor(removeNewLine(approvedFor));

				String restrictions = "";
				if (page.contains("Restri��es/Limita��es: "))
					restrictions = page.substring(page.indexOf("Restri��es/Limita��es: ") + 23,
							page.contains("Observa��o: ") ? page.indexOf("Observa��o: ") - 1
									: page.indexOf("Marca��o do CA: ") - 1);
				ca.setRestrictions(removeNewLine(restrictions));

				String observation = "";
				if (page.contains("Observa��o: "))
					observation = page.substring(page.indexOf("Observa��o: ") + 12,
							page.indexOf("Marca��o do CA: ") - 1);
				ca.setObservation(removeNewLine(observation));

				if (page.contains("Marca��o do CA:")) {
					String caLocation = page.substring(page.indexOf("Marca��o do CA: ") + 16,
							page.indexOf("Refer�ncias: ") - 1);
					ca.setCaLocation(removeNewLine(caLocation));
				}

				boolean hasTechnician = page.contains("Respons�vel T�cnico:");
				String references = "";

				if (page.contains("Refer�ncias:")) {
					if (hasTechnician)
						references = page.substring(page.indexOf("Refer�ncias:") + 13,
								page.indexOf("Respons�vel T�cnico:") - 1);
					else
						references = page.substring(page.indexOf("Refer�ncias:") + 13, (page.indexOf("Tamanhos:") == -1
								? page.indexOf("Normas t�cnicas: ") - 1 : page.indexOf("Tamanhos: ") - 1));
					ca.setReferences(removeNewLine(references));
				}

				String size = "";
				if (page.contains("Tamanhos: ")) {
					if (hasTechnician)
						size = page.substring(page.indexOf("Tamanhos: ") + 10, page.indexOf("Cores: ") == -1
								? (page.indexOf("Respons�vel T�cnico:") - 1) : page.indexOf("Cores") - 1);
					else
						size = (page.substring(page.indexOf("Tamanhos: ") + 10, page.indexOf("Cores: ") == -1
								? (page.indexOf("Normas t�cnicas: ") - 1) : (page.indexOf("Cores: ") - 1)));

					ca.setSize(removeNewLine(size));
				}

				String colors = "";
				if (page.contains("Cores: "))
					colors = page.substring(page.indexOf("Cores: ") + 7,
							page.indexOf("Marca��o do selo do Inmetro: ") == -1
									? (page.indexOf("Normas t�cnicas: ") - 1)
									: (page.indexOf("Marca��o do selo do Inmetro: ") - 1));
				ca.setColors(removeNewLine(colors));

				String technician = "";
				if (hasTechnician)
					technician = page.substring(page.indexOf("Respons�vel T�cnico:") + 21,
							page.indexOf("N� Registro Profissional:") - 1);
				ca.setTechnician(removeNewLine(technician));

				String professionalRegistration = "";
				if (page.contains("N� Registro Profissional:"))
					professionalRegistration = page.substring(page.indexOf("N� Registro Profissional: ") + 26,
							page.indexOf("Empresa:") - 1);
				ca.setProfessionalRegistration(removeNewLine(professionalRegistration));

				String inmetroSticker = "";
				if (page.contains("Marca��o do selo do Inmetro: "))
					inmetroSticker = page.substring(page.indexOf("Marca��o do selo do Inmetro: ") + 29,
							page.indexOf("Atestado de conformidade Inmetro:") - 1);
				ca.setInmetroSticker(removeNewLine(inmetroSticker));

				String inmetroConformityProof = "";
				if (page.contains("Atestado de conformidade Inmetro: "))
					inmetroConformityProof = page.substring(page.indexOf("Atestado de conformidade Inmetro: ") + 34,
							page.indexOf("Normas t�cnicas: ") - 1);
				ca.setInmetroConformityProof(removeNewLine(inmetroConformityProof));

				String techRules = "";
				if (page.contains("Normas t�cnicas: ")) {
					techRules = page.substring(page.indexOf("Normas t�cnicas: ") + 17,
							page.indexOf("Laudos:") == -1 ? page.indexOf("Empresa: ") - 1 : page.indexOf("Laudos:"));
				}
				String[] techRulesArray = removeNewLine(techRules).split(";");
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
