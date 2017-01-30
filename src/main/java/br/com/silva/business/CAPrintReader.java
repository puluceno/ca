package br.com.silva.business;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.pmw.tinylog.Logger;

import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import br.com.silva.exceptions.InvalidCAException;
import br.com.silva.model.CA;

public class CAPrintReader {

	// private static String LN = "\n";
	public static String LN = System.getProperty("line.separator");
	private static final String END_OF_FILE = LN + "Todos os direitos reservados";

	public static void main(String[] args) throws Exception {
		// List<String> failed = new ArrayList<String>();
		// try (Stream<Path> paths =
		// Files.walk(Paths.get("/home/pulu/Documents/CA print pdf"))) {
		// paths.forEach(filePath -> {
		// if (Files.isRegularFile(filePath)) {
		// try {
		// failed.add(readPDF(filePath.toString()));
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// });
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// failed.removeAll(Collections.singleton(null));
		// for (String string : failed) {
		// System.err.println(readPDF(string));
		// }

		// System.out.println(readPDF("/home/pulu/Documents/CA print pdf/bota Ca
		// 25294.pdf"));
	}

	public static CA readPDF(String pathToPDF) throws Exception {
		CA ca = new CA();

		try {
			PdfReader reader = new PdfReader(pathToPDF);
			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				String page = PdfTextExtractor.getTextFromPage(reader, i);
				String cnpjRegex = "(\\d{2}.\\d{3}.\\d{3}\\/\\d{4}-\\d{2})";
				Matcher m = Pattern.compile(cnpjRegex).matcher(page);

				boolean hasNumber = page.contains(LN + "Nº do CA:");
				boolean hasNumber2 = page.contains(LN + "Nº do CA:");
				boolean hasStatus = page.contains(" Situação:");
				boolean hasDate = page.contains(LN + "Validade:");
				boolean hasProcessNumber = page.contains(LN + "Nº do Processo:");
				boolean hasCNPJ = m.find();
				boolean hasCompany = page.contains(" Razão Social:");
				boolean hasOrigin = page.contains(LN + "Natureza:");
				boolean hasEquipment = page.contains(LN + "Equipamento:");
				boolean hasDescription = page.contains(LN + "Descrição do Equipamento:");
				boolean hasApprovedFor = StringUtils.containsIgnoreCase(page, "Aprovado Para:");
				boolean hasApprovedForBroken = page.contains(LN + "Para:");
				boolean hasCaLocation = page.contains(LN + "Marcação do CA:");
				boolean hasReferences = page.contains(LN + "Referências:");
				boolean hasSize = page.contains(LN + "Tamanho:");
				boolean hasColor = page.contains(LN + "Cor:");
				boolean hasTechnician = page.contains(LN + "Responsável Técnico:");
				boolean hasProfessionalRegistration = page.contains(LN + "Registro Profissional:");
				boolean hasART = page.contains(" ART:");
				boolean hasReport = page.contains("Nº. do Laudo Laboratório Razão Social");
				boolean hasTechRules = page.contains(LN + "Norma");
				boolean hasAttenuationTable = page.contains("Tabela de Atenuação");

				if (hasNumber) {
					String number = page
							.substring(page.indexOf(LN + "Nº do CA:") + 11, page.indexOf(LN + "Nº do CA: ") + 16)
							.replaceAll("[^\\d]", "");
					ca.setNumber(removeNewLine(number));
				}

				if (!hasNumber && hasNumber2) {
					String number = page.substring(page.indexOf(LN + "Nº do CA:") + 10, page.indexOf("Situação:") - 1)
							.replaceAll("[^\\d]", "");
					ca.setNumber(removeNewLine(number));
				}

				if (hasStatus) {
					String status = page.substring(page.indexOf("Situação: ") + 10, page.indexOf(LN + "Validade:"));
					ca.setStatus(removeNewLine(status));
				}

				if (hasDate) {
					String date = page.substring(page.indexOf(LN + "Validade:") + 10,
							page.indexOf(LN + "Nº do Processo: "));
					if (date.isEmpty())
						throw new InvalidCAException("No date found!");
					ca.setDate(removeNewLine(date));
				}

				if (hasProcessNumber) {
					String processNumber = page.substring(page.indexOf(LN + "Nº do Processo: ") + 17,
							StringUtils.ordinalIndexOf(page, "Nº do", 3) - 1);
					ca.setProcessNumber(removeNewLine(processNumber));
				}

				if (hasCNPJ) {
					String cnpj = m.group(1);
					ca.setCnpj(removeNewLine(cnpj));
				}

				if (hasCompany) {
					String company = page.substring(page.indexOf(" Razão Social: ") + 15, page.indexOf(LN + "CNPJ:"));
					ca.setCompany(removeNewLine(company));
				}

				if (hasOrigin) {
					String origin = page.substring(page.indexOf(LN + "Natureza: ") + 11,
							page.indexOf(LN + "Equipamento:"));
					ca.setOrigin(removeNewLine(origin));
				}

				if (hasEquipment) {
					String equipment = page.substring(page.indexOf(LN + "Equipamento:") + 13,
							page.indexOf(LN + "Descrição do Equipamento:"));
					ca.setEquipment(removeNewLine(equipment));
				}

				if (hasDescription) {
					String description = "";
					if (page.contains("Dados Complemantares"))
						description = page.substring(page.indexOf(LN + "Descrição do Equipamento:") + 27,
								page.indexOf("Dados Complemantares") - 1);
					else
						description = page.substring(page.indexOf(LN + "Descrição do Equipamento:") + 27,
								page.indexOf(LN + " Laudo"));
					ca.setDescription(removeNewLine(description));
				}

				if (hasCaLocation) {
					String caLocation = page.substring(page.indexOf(LN + "Marcação do CA:") + 16,
							page.indexOf(LN + "Referências:"));
					ca.setCaLocation(removeNewLine(caLocation));
				}

				if (hasReferences) {
					String references = "";
					if (hasSize)
						references = page.substring(page.indexOf(LN + "Referências:") + 13,
								page.indexOf(LN + "Tamanho:"));
					else if (page.contains(LN + " Termo"))
						page.substring(page.indexOf(LN + "Referências:") + 13, page.indexOf(LN + " Termo"));
					else if (page.contains(LN + "Laudo"))
						page.substring(page.indexOf(LN + "Referências:") + 13, page.indexOf(LN + "Laudo"));
					ca.setReferences(removeNewLine(references));
				}

				if (hasSize) {
					String size = "";
					if (hasColor)
						size = page.substring(page.indexOf(LN + "Tamanho:") + 9, page.indexOf(LN + "Cor:"));
					else
						size = page.substring(page.indexOf(LN + "Tamanho:") + 9, page.indexOf(LN + "Laudo"));
					ca.setSize(removeNewLine(size));
				}

				if (hasApprovedFor) {
					String approvedFor = "";
					if (hasTechnician) {
						approvedFor = page.substring(page.indexOf(LN + "Aprovado para:") + 15,
								page.indexOf(LN + "Responsável Técnico:"));
					} else {
						approvedFor = page.substring(page.indexOf(LN + "Aprovado Para:") + 15,
								page.indexOf(END_OF_FILE));
					}
					ca.setApprovedFor(removeNewLine(approvedFor));
				}

				if (hasApprovedForBroken) {
					String approvedFor = "";
					if (hasReport)
						approvedFor = page
								.substring(page.indexOf(LN + "Aprovado ") + 10,
										page.indexOf(LN + "Nº. do Laudo Laboratório Razão Social"))
								.replace("Para:", "");
					else if (!hasTechnician && !hasProfessionalRegistration && !hasTechRules)
						approvedFor = page.substring(page.indexOf(LN + "Aprovado ") + 10,
								page.indexOf(LN + "Todos os direitos reservados"));
					else
						approvedFor = page.substring(page.indexOf(LN + "Aprovado ") + 10, page.indexOf(LN + "Para:"));
					ca.setApprovedFor(removeNewLine(approvedFor));
				}

				if (hasTechnician) {
					String technician = page.substring(page.indexOf(LN + "Responsável Técnico:") + 21,
							page.indexOf(LN + "Registro Profissional:"));
					ca.setTechnician(removeNewLine(technician));
				}

				if (hasProfessionalRegistration) {
					String professionalRegistration = "";
					if (hasART)
						professionalRegistration = page.substring(page.indexOf(LN + "Registro Profissional:") + 23,
								page.indexOf(" ART"));
					else
						professionalRegistration = page.substring(page.indexOf(LN + "Registro Profissional:") + 23,
								page.indexOf(END_OF_FILE));
					ca.setProfessionalRegistration(removeNewLine(professionalRegistration));
				}

				if (hasTechRules) {
					String techRules = "";
					if (page.contains(END_OF_FILE))
						techRules = page.substring(page.lastIndexOf(LN + "Norma" + LN) + 7,
								page.lastIndexOf(END_OF_FILE));
					else if (page.contains(LN + "1 de 2"))
						techRules = page.substring(page.lastIndexOf(LN + "Norma" + LN) + 7,
								page.lastIndexOf(LN + "1 de 2"));
					else if (page.contains(LN + "http://www3.mte.gov.br"))
						techRules = page.substring(page.lastIndexOf(LN + "Norma" + LN) + 7,
								page.lastIndexOf(LN + "http://www3.mte.gov.br"));
					String[] techRulesArray = removeNewLine(techRules).split(LN);
					ca.setTechnicalRules(Arrays.asList(techRulesArray));
				}

				if (hasAttenuationTable) {
					Map<String, String[]> attenuationTable = new HashMap<String, String[]>();

					String[] dbs = page
							.substring(page.indexOf("Atenuação db:") + 13, page.indexOf("Desvio Padrão:") - 1)
							.split(" ", 10);

					String[] devs = page.substring(page.indexOf("Desvio Padrão:") + 14, page.lastIndexOf(LN)).split(" ",
							10);

					attenuationTable.put("dbAttenuations", dbs);
					attenuationTable.put("deviations", devs);
					ca.setAttenuationTable(attenuationTable);
				}

			}
		} catch (Exception e) {
			if (e instanceof InvalidPdfException || e instanceof InvalidCAException)
				throw e;
			Logger.trace(e, "CA file " + pathToPDF);
		}
		return ca;
	}

	static String removeNewLine(String string) {
		return string.replace(LN, " ").replace("\r", " ").trim();
	}
}
