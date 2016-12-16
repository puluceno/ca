package br.com.silva.data;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.pmw.tinylog.Logger;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;

import br.com.silva.resources.MongoResource;

public class CARepository {
	private static MongoCollection<Document> caCollection = MongoResource.getCollection("ca", "ca");
	private static MongoCollection<Document> updateCollection = MongoResource.getDataBase("ca").getCollection("update");

	public static Document findCA(Document query, String... fields) {
		Document ca;
		if (fields != null && fields.length > 0) {
			ca = caCollection.find(query).projection(fields(include(fields), excludeId())).first();
		} else {
			ca = caCollection.find(query).limit(1).first();
		}

		return ca;
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	public static ArrayList<Document> findCAList(Document query) {
		if (query.isEmpty())
			return caCollection.find().limit(100).into(new ArrayList<Document>());
		List<Bson> regexes = new ArrayList<Bson>();

		query.keySet().forEach(key -> {
			query.put(key, ".*" + query.getString(key) + ".*");
			regexes.add(regex(key, query.getString(key), "i"));
		});

		return caCollection.find(and(regexes)).sort(ascending("number")).limit(100).into(new ArrayList<Document>());
	}

	public static void addToUpdate(Map<String, String> updateData) {
		updateData.forEach((k, v) -> {
			Document query = new Document("number", k).append("date", v);
			Document ca = findCA(query, "number");
			if (ca == null) {
				updateCollection.insertOne(query);
			}
		});
	}

	public static void createIndex(String collection, String field) {
		try {
			Logger.info("Indexing the colletion '{}' for field '{}'.", collection, field);
			MongoResource.getDataBase("ca").getCollection(collection).createIndex(new Document(field, 1));
		} catch (Exception e) {
			if (e instanceof MongoCommandException)
				Logger.error("Could not create index: key too large to index.");
			else
				Logger.trace(e);
		}
	}
}
