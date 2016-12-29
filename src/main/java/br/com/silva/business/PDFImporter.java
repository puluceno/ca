package br.com.silva.business;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
import br.com.silva.exceptions.InvalidCAException;
import br.com.silva.model.CA;
import br.com.silva.model.CAParser;
import br.com.silva.resources.MongoResource;
import br.com.silva.tools.MaskTools;

public class PDFImporter {

	private static final String PDF_EXTENSION = ".pdf";
	// private static final String CA_FOLDER = System.getProperty("user.home") +
	// File.separator + "Documents"
	// + File.separator + "CAs" + File.separator;
	private static final String CA_FOLDER = "C:" + File.separator + "xampp" + File.separator + "htdocs" + File.separator
			+ "CAs" + File.separator;

	private static MongoCollection<Document> caCollection = MongoResource.getDataBase("ca").getCollection("ca");
	private static MongoCollection<Document> caStatusCollection = MongoResource.getDataBase("ca")
			.getCollection("castatus");
	private static MongoCollection<Document> updateCollection = MongoResource.getDataBase("ca").getCollection("update");

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

		Logger.info("Importing {} CA into the database...", files.size());
		for (String file : files) {
			try {
				CA ca = CAReader.readPDF(file);

				caCollection.insertOne(CAParser.toDocument(ca).append("fileName",
						file.substring(file.lastIndexOf(File.separator) + 1, file.length())));

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

	public static String saveAndImportCA(String body) {
		File file = null;
		CA ca;

		try {
			file = readUploadedFile(body);
			ca = CAReader.readPDF(file.getAbsolutePath());
			if (ca.getNumber() == null)
				throw new InvalidCAException();
			String number = ca.getNumber();

			if (CARepository.findCA(new Document("number", number).append("date", ca.getDate()), "number") == null) {

				File newFileName = new File(CA_FOLDER + number + "_" + (ca.getDate().contains("Condicionada")
						? MaskTools.unMaskProcessNumber(ca.getProcessNumber()) : MaskTools.unMaskDate(ca.getDate()))
						+ PDF_EXTENSION);
				boolean renamed = file.renameTo(newFileName);

				if (renamed) {
					caCollection.insertOne(CAParser.toDocument(ca).append("fileName", newFileName.getName()));

					caStatusCollection
							.updateOne(
									eq("number", number), combine(set("number", number), set("exist", true),
											set("downloaded", true), set("imported", true)),
									new UpdateOptions().upsert(true));

					updateCollection.deleteOne(eq("number", number));

					Logger.info("CA {} found and saved under the file {}.", number, newFileName);

				} else {
					caStatusCollection
							.updateOne(
									eq("number", number), combine(set("number", number), set("exist", true),
											set("downloaded", true), set("imported", false)),
									new UpdateOptions().upsert(true));

					updateCollection.deleteOne(eq("number", number));

					Logger.info("Not imported! CA {} found and saved under the file {}.", number, newFileName);
				}
				return "CA inserido com sucesso!";
			} else
				return "CA já existe no sistema.";
		} catch (Exception e) {
			Logger.trace(e);
			return "O arquivo enviado não é válido.";
		} finally {
			file.delete();
		}
	}

	private static File readUploadedFile(String body) throws FileNotFoundException, IOException {
		File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "temp.pdf");
		InputStream is = new ByteArrayInputStream(body.getBytes(StandardCharsets.ISO_8859_1));
		OutputStream os = new FileOutputStream(file);
		byte[] bytes = new byte[1024];
		int read;
		while ((read = is.read(bytes)) != -1) {
			os.write(bytes, 0, read);
		}
		os.close();
		is.close();
		return file;
	}
}