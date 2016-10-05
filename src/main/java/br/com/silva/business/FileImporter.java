package br.com.silva.business;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.pmw.tinylog.Logger;

import com.mongodb.client.MongoCollection;

import br.com.silva.Tools.CAParser;
import br.com.silva.Tools.FileTools;
import br.com.silva.Tools.MaskTools;
import br.com.silva.Tools.ReportParser;
import br.com.silva.model.CA;
import br.com.silva.model.Report;
import br.com.silva.resources.MongoResource;
import br.com.silva.service.CAService;

public class FileImporter {

	private static String fileURL = "http://www3.mte.gov.br/sistemas/CAEPI_Arquivos/tgg_export_caepi.zip";
	private static String fileLocation = System.getProperty("user.home") + File.separator + "Documents";
	private static MongoCollection<Document> caCollection = MongoResource.getCollection("ca", "ca");

	public static void main(String[] args) throws IOException {
		long begin = new Date().getTime();

		FileTools.downloadFile(new URL(fileURL), fileLocation + "/caepi.zip");

		FileTools.unzipFile(fileLocation + "/caepi.zip", fileLocation + "/caepi.txt");

		readFileAndInsert(fileLocation + "/caepi.txt");
		createIndex("number");

		Logger.info("Finished importing process. Elapsed time: {}", ((new Date().getTime() - begin) / 1000) + "s.");
	}

	/**
	 * 
	 * @param field
	 */
	private static void createIndex(String field) {
		Logger.info("Indexing the database using {} for better performance...", field);
		caCollection.createIndex(new Document(field, 1));
	}

	/**
	 * 
	 * @param fileName
	 */
	public static void readFileAndInsert(String fileName) {
		Logger.info("Reading file {}", fileName);
		try {
			File fileDir = new File(fileName);

			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "ISO8859_1"));

			String str;
			int i = 0;
			while ((str = in.readLine()) != null) {
				insertCA(textToObject(str));
				i++;
			}

			in.close();
			Logger.info("{} CAs read succesfully", i);
		} catch (Exception e) {
			Logger.trace(e);
		}
	}

	/**
	 * 
	 * @param line
	 * @return
	 */
	private static CA textToObject(String line) {
		try {
			String[] split = line.split("\\|");

			List<String> rules = new ArrayList<String>();
			if (split.length == 19)
				rules.add(split[18]);
			else
				rules.add("");

			List<Report> reports = new ArrayList<Report>();
			reports.add(new Report(MaskTools.maskCNPJ(split[15]), split[16], split[17]));

			return new CA(split[0], split[1], split[2], MaskTools.maskProcessNumber(split[3]),
					MaskTools.maskCNPJ(split[4]), split[5], split[6], split[7], split[8], split[9], split[10],
					split[11], reports, split[12], split[13], split[14], rules);

		} catch (Exception e) {
			Logger.error("Failed to read line : {}", line);
			Logger.trace(e);
			return null;
		}
	}

	/**
	 * @param caObj
	 */
	private static void insertCA(CA caObj) {
		Document document = CAParser.toDocument(caObj);

		CA ca = CAParser.toObject(CAService.findCA(new Document("number", caObj.getNumber())
				.append("processNumber", caObj.getProcessNumber()).append("approvedFor", caObj.getApprovedFor())
				.append("description", caObj.getDescription()).append("status", caObj.getStatus())));

		if (ca == null)
			caCollection.insertOne(document);
		else {
			ca.getTechnicalRules().removeAll(caObj.getTechnicalRules());
			ca.getTechnicalRules().addAll(caObj.getTechnicalRules());

			ca.getReports().removeAll(caObj.getReports());
			ca.getReports().addAll(caObj.getReports());

			caCollection.findOneAndUpdate(eq("number", caObj.getNumber()),
					combine(set("technicalRules", ca.getTechnicalRules()),
							set("reports", ReportParser.toDocument(ca.getReports()))));
		}

	}

}
