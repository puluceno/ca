package br.com.silva.business;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import br.com.silva.Tools.CAParser;
import br.com.silva.Tools.MaskTools;
import br.com.silva.Tools.ReportParser;
import br.com.silva.model.CA;
import br.com.silva.model.Report;
import br.com.silva.resources.MongoResource;

public class FileImporter {

	private static String fileURL = "http://www3.mte.gov.br/sistemas/CAEPI_Arquivos/tgg_export_caepi.zip";
	private static String fileLocation = System.getProperty("user.home") + File.separator + "Documents";
	private static MongoCollection<Document> caCollection = MongoResource.getCollection("ca", "ca");

	public static void main(String[] args) throws IOException {

		// FileTools.downloadFile(new URL(fileURL), fileLocation +
		// "/caepi.zip");

		// FileTools.unzipFile(fileLocation + "/caepi.zip", fileLocation +
		// "/caepi.txt");
		// caCollection.drop();

		long begin = new Date().getTime();
		readFileAndInsert(fileLocation + "/caepi.txt");

		System.out.println("Finished importing file. Elapsed time: " + ((new Date().getTime() - begin) / 1000) + "s.");
	}

	public static void readFileAndInsert(String fileName) {
		try {
			File fileDir = new File(fileName);

			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "ISO8859_1"));

			String str;

			while ((str = in.readLine()) != null) {
				insertCA(textToObject(str));
			}

			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
			e.printStackTrace();
			System.err.println("Failed to read line : " + line);
			return null;
		}
	}

	/**
	 * @param caObj
	 */
	private static void insertCA(CA caObj) {
		Document document = CAParser.toDocument(caObj);

		CA ca = findCA(new Document("number", caObj.getNumber()));
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

	private static CA findCA(Document query) {
		Document first = caCollection.find(query).first();
		return CAParser.toObject(first);
	}
}
