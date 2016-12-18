package br.com.silva.business;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.bson.Document;
import org.pmw.tinylog.Logger;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import br.com.silva.data.CARepository;
import br.com.silva.model.CA;
import br.com.silva.model.CAParser;
import br.com.silva.resources.MongoResource;

public class PDFImporter {

	// private static final String CA_FOLDER = "/home/pulu/Documents/CAs";
	private static final String CA_FOLDER = "C:" + File.separator + "xampp" + File.separator + "htdocs" + File.separator
			+ "CAs";

	private static MongoCollection<Document> caCollection = MongoResource.getDataBase("ca").getCollection("ca");
	private static MongoCollection<Document> caStatusCollection = MongoResource.getDataBase("ca")
			.getCollection("castatus");

	public static void main(String[] args) {
		importAllPDF();
	}

	public static void importAllPDF() {
		caCollection.drop();
		caStatusCollection.drop();
		long beginCA = new Date().getTime();
		Set<String> files = new HashSet<String>();
		try (Stream<Path> paths = Files.walk(Paths.get(CA_FOLDER))) {
			paths.forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					files.add(filePath.toString());
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		CARepository.createIndex("ca", "number");
		CARepository.createIndex("ca", "equipment");
		CARepository.createIndex("ca", "date");
		CARepository.createIndex("castatus", "number");

		for (String file : files) {
			try {
				CA ca = CAReader.readPDF(file);

				caCollection.insertOne(
						CAParser.toDocument(ca).append("fileName", file.replace(CA_FOLDER + File.separator, "")));

				caStatusCollection
						.updateOne(
								eq("number", ca.getNumber()), combine(set("number", ca.getNumber()), set("exist", true),
										set("downloaded", true), set("imported", true)),
								new UpdateOptions().upsert(true));
			} catch (Exception e) {
				int end = file.indexOf("_") == -1 ? file.indexOf(".pdf") : file.indexOf("_");
				int number = Integer.parseInt(file.substring(CA_FOLDER.length() + 1, end));
				caStatusCollection.updateOne(
						eq("number", number), combine(set("number", number), set("exist", true),
								set("downloaded", true), set("imported", false), set("retry", true)),
						new UpdateOptions().upsert(true));
			}
		}

		Logger.info("Operation finished in " + (new Date().getTime() - beginCA) + "ms");
	}
}