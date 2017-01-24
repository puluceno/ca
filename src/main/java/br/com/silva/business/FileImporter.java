package br.com.silva.business;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.pmw.tinylog.Logger;

import com.mongodb.client.MongoCollection;

import br.com.silva.crawler.CAEPIDownloader;
import br.com.silva.data.CARepository;
import br.com.silva.data.ParamsRepository;
import br.com.silva.data.UpdateRepository;
import br.com.silva.model.CAConstants;
import br.com.silva.resources.MongoResource;
import br.com.silva.tools.FileTools;
import br.com.silva.tools.MaskTools;

/**
 * Imports the CAEPI.txt file
 * 
 * @author pulu
 *
 */
public class FileImporter {

	public static void main(String[] args) {
		FileImporter.importCAList();
	}

	private static MongoCollection<Document> updateCollection = MongoResource.getDataBase("ca").getCollection("update");

	public static void scheduleImport() {
		TimerTask repeatedTask = new TimerTask() {
			@Override
			public void run() {
				try {
					importCAList();
					CAEPIDownloader.crawlCAS();
				} catch (Exception e) {
					Logger.trace(e);
				}
			}
		};
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(repeatedTask, 0, 10, TimeUnit.DAYS);
	}

	public static void importCAList() {
		long begin = new Date().getTime();
		try {
			FileTools.downloadFile(new URL(ParamsRepository.findParams().getString("fileUrl")),
					CAConstants.HOME_DIR + "caepi.zip");
		} catch (Exception e) {
			Logger.error("File does not exist in the given location.");
			Logger.trace(e);
		}

		FileTools.unzipFile(CAConstants.HOME_DIR + "caepi.zip", CAConstants.HOME_DIR + "caepi.txt");
		updateCollection.drop();
		readFileAndInsert(CAConstants.HOME_DIR + "caepi.txt");
		ParamsRepository.updateParams(new Date());

		cleanUp();
		Logger.info("Finished importing process. Elapsed time: {}", ((new Date().getTime() - begin) / 1000) + "s.");
	}

	/**
	 * 
	 * @param fileName
	 */
	public static void readFileAndInsert(String fileName) {
		Logger.info("Reading file {}", fileName);
		BufferedReader in = null;
		try {
			File fileDir = new File(fileName);

			in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "ISO8859_1"));
			String str;
			Set<Document> data = new HashSet<Document>();
			Document query = new Document();
			while ((str = in.readLine()) != null) {
				if (!str.contains("#NRRegistroCA")) {
					String[] split = str.split("\\|");
					query.append("number", split[0]);
					if (split[1] != null && !split[1].isEmpty())
						query.append("date", split[1]);
					if (split[3] != null && split[3].length() > 14) {
						query.append("processNumber", MaskTools.maskProcessNumber(split[3]));
					}
					if (CARepository.findCA(query, "number") == null) {
						data.add(new Document("number", split[0]).append("processNumber", split[3]));
					}
				}
				query.clear();
			}
			in.close();

			UpdateRepository.insertList(new ArrayList<Document>(data));
			Logger.info("{} CAs added to update list", updateCollection.count());
		} catch (Exception e) {
			Logger.trace(e);
		}
	}

	private static void cleanUp() {
		FileTools.deleteFile(CAConstants.HOME_DIR + "caepi.zip");
		FileTools.deleteFile(CAConstants.HOME_DIR + "caepi.txt");
	}

}
