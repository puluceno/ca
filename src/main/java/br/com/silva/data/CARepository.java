package br.com.silva.data;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.pmw.tinylog.Logger;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;

import br.com.silva.resources.MongoResource;

public class CARepository {
	private static MongoCollection<Document> caCollection = MongoResource.getDataBase("ca").getCollection("ca");

	public static Document findCA(Document query, String... fields) {
		Document ca;
		if (fields != null && fields.length > 0) {
			ca = caCollection.find(query).projection(fields(include(fields), excludeId())).first();
		} else {
			ca = caCollection.find(query).limit(1).first();
		}

		return ca;
	}

	public static ArrayList<Document> findCAList(Document query) {
		if (query.isEmpty()) {
			ArrayList<Document> allCA = caCollection.find().limit(100).into(new ArrayList<Document>());
			allCA.add(new Document("count", count()));
			return allCA;
		}
		List<Bson> regexes = new ArrayList<Bson>();

		query.keySet().forEach(key -> {
			query.put(key, ".*" + query.getString(key) + ".*");
			regexes.add(regex(key, query.getString(key), "i"));
		});

		ArrayList<Document> cas = caCollection.find(and(regexes)).limit(100).sort(ascending("number"))
				.into(new ArrayList<Document>());
		cas.add(new Document("count", (int) caCollection.count(and(regexes))));
		return cas;
	}

	public static int count() {
		return (int) caCollection.count();
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

	public static void createCoumpoundIndex(String collection, String... fields) {
		try {
			Document index = new Document();
			for (int i = 0; i < fields.length; i++) {
				index.append(fields[i], 1);
			}
			MongoResource.getDataBase("ca").getCollection(collection).createIndex(index);
			Logger.info("Index created in the colletion '{}' for field '{}'.", collection, index.entrySet());
		} catch (Exception e) {
			if (e instanceof MongoCommandException)
				Logger.error("Could not create index: key too large to index.");
			else
				Logger.trace(e);
		}
	}
}
