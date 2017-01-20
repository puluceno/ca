package br.com.silva.data;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Part;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.pmw.tinylog.Logger;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import br.com.silva.resources.MongoResource;
import br.com.silva.tools.FileTools;
import spark.Request;

public class DurabilityRepository {
	public static final String UPLOAD_DIR = "C:" + File.separator + "xampp" + File.separator + "htdocs" + File.separator
			+ "files" + File.separator;

	private static MongoCollection<Document> durabilityCollection = MongoResource.getDataBase("ca")
			.getCollection("durability");

	public static List<Document> findAll() {
		return durabilityCollection.find().into(new ArrayList<Document>());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean readAndSave(Request req) {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(req.raw().getPart("data").getInputStream(), StandardCharsets.UTF_8));
			Document durability = new Gson().fromJson(reader, Document.class);

			String equipment = durability.getString("equipment");
			int days = durability.getDouble("days").intValue();

			StringBuilder fileName = new StringBuilder(durability.getString("equipment").replaceAll(" ", "_"));

			List<LinkedTreeMap> material = (List<LinkedTreeMap>) durability.get("material");
			List<Document> materials = new ArrayList<Document>();
			material.forEach(doc -> {
				String materialText = (String) doc.get("text");
				MaterialRepository.checkAndInsert(materialText.toLowerCase());
				fileName.append("_" + materialText.replaceAll(" ", "_"));
				materials.add(new Document("text", materialText));
			});

			Part file = req.raw().getPart("file");
			String submittedFileName = file.getSubmittedFileName();
			Bson setFileName = null;
			if (submittedFileName != null) {
				FileTools.saveUploadedFile(req, fileName.toString().toLowerCase(), UPLOAD_DIR);
				setFileName = set("fileName", fileName.toString().toLowerCase() + ".pdf");
			}
			LinkedTreeMap _id = (LinkedTreeMap) durability.get("_id");

			ObjectId id;
			if (_id != null) {
				id = new ObjectId((String) _id.get("$oid"));
			} else {
				id = new ObjectId();
			}

			Bson combine = null;
			if (setFileName != null)
				combine = combine(set("created", new Date()), set("equipment", equipment), set("material", materials),
						set("days", days), setFileName);
			else
				combine = combine(set("created", new Date()), set("equipment", equipment), set("material", materials),
						set("days", days));

			durabilityCollection.updateOne(eq("_id", id), combine, new UpdateOptions().upsert(true));
			return true;
		} catch (Exception e) {
			Logger.trace(e);
			return false;
		}

	}

	public static boolean delete(String id) {
		FileTools.deleteFile(FileTools.UPLOAD_DIR + durabilityCollection.find(new Document("_id", new ObjectId(id)))
				.projection(fields(include("fileName"), excludeId())).first().getString("fileName"));
		return durabilityCollection.deleteOne(new Document("_id", new ObjectId(id))).wasAcknowledged();
	}
}
