package br.com.silva.business;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.pmw.tinylog.Logger;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;

import br.com.silva.data.CARepository;
import br.com.silva.data.ParamsRepository;
import br.com.silva.resources.MongoResource;

/**
 * Imports the CAEPI.txt file
 * 
 * @author pulu
 *
 */
public class FileImporter {

	public static void main(String[] args) {
		FileImporter.importCAFile();
	}

	private static String fileLocation = System.getProperty("user.home") + File.separator + "Documents";
	private static MongoCollection<Document> caCollection = MongoResource.getCollection("ca", "ca");
	private static MongoCollection<Document> updateCollection = MongoResource.getDataBase("ca").getCollection("update");

	public static void scheduleImport() {
		TimerTask repeatedTask = new TimerTask() {
			@Override
			public void run() {
				try {
					importCAFile();
				} catch (Exception e) {
					Logger.trace(e);
				}
			}
		};
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(repeatedTask, 0, 10, TimeUnit.DAYS);
	}

	public static void importCAFile() {
		long begin = new Date().getTime();
		try {
			// FileTools.downloadFile(new
			// URL(CAService.findParams().getString("fileUrl")),
			// fileLocation + File.separator + "caepi.zip");
		} catch (Exception e) {
			Logger.error("File does not exist in the given location.");
			Logger.trace(e);
		}

		// FileTools.unzipFile(fileLocation + File.separator + "caepi.zip",
		// fileLocation + File.separator + "caepi.txt");

		readFileAndInsert(fileLocation + File.separator + "caepi.txt");
		createIndex("number");
		createIndex("company");
		ParamsRepository.updateParams(new Date());

		Logger.info("Finished importing process. Elapsed time: {}", ((new Date().getTime() - begin) / 1000) + "s.");
	}

	/**
	 * 
	 * @param field
	 */
	private static void createIndex(String field) {
		try {
			Logger.info("Indexing the database using {} for better performance...", field);
			caCollection.createIndex(new Document(field, 1));
		} catch (Exception e) {
			if (e instanceof MongoCommandException)
				Logger.error("Could not create index: key too large to index.");
			else
				Logger.trace(e);
		}
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
			updateCollection.drop();
			String str;
			while ((str = in.readLine()) != null) {
				if (!str.contains("#NRRegistroCA"))
					CARepository.findAndInsertCA(extractData(str));
			}

			in.close();

			Logger.info("All CAs read succesfully!");
			Logger.info("{} CAs added to update list", updateCollection.count());
		} catch (Exception e) {
			Logger.trace(e);
		}
	}

	/**
	 * 
	 * @param line
	 * @return
	 */
	private static Document extractData(String line) {
		try {
			String[] split = line.split("\\|");

			Document query = new Document("number", split[0].replaceAll("[^\\d]", "")).append("date", split[1]);
			// if (split.length >= 8 && split[7] != null)
			// query.append("equipment", CAReader.removeNewLine(split[7]));
			// if (split.length >= 9 && split[8] != null)
			// query.append("description", CAReader.removeNewLine(split[8]));
			// if (split.length >= 13 && split[12] != null)
			// query.append("approvedFor", CAReader.removeNewLine(split[12]));

			return query;

		} catch (Exception e) {
			Logger.error("Failed to read line : {}", line);
			Logger.trace(e);
			return null;
		}
	}

}
