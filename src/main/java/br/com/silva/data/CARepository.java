package br.com.silva.data;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;

import br.com.silva.resources.MongoResource;

public class CARepository {
	private static MongoCollection<Document> caCollection = MongoResource.getDataBase("ca").getCollection("ca");

	public static Document findCA(Document query, String... fields) {
		Document ca;
		if (fields != null && fields.length > 0) {
			ca = caCollection.find(and(query, exists("removed", false)))
					.projection(fields(include(fields), excludeId())).first();
		} else {
			ca = caCollection.find(and(query, exists("removed", false))).limit(1).first();
		}

		return ca;
	}

	public static ArrayList<Document> findCAList(Document query) {
		if (query.isEmpty()) {
			ArrayList<Document> allCA = caCollection.find(exists("removed", false)).limit(200).sort(descending("date"))
					.into(new ArrayList<Document>());
			allCA.add(new Document("count", count()));
			return allCA;
		}
		Boolean exactSearch = Boolean.valueOf(query.getString("exactSearch"));
		if (exactSearch) {
			query.remove("exactSearch");
			ArrayList<Document> exact = caCollection.find(and(query, exists("removed", false)))
					.into(new ArrayList<Document>());
			exact.add(new Document("count", (int) caCollection.count(query)));
			return exact;
		}
		List<Bson> regexes = new ArrayList<Bson>();

		query.keySet().forEach(key -> {
			query.put(key, ".*" + query.getString(key) + ".*");
			regexes.add(regex(key, query.getString(key), "i"));
		});
		regexes.add(exists("removed", false));

		ArrayList<Document> cas = caCollection.find(and(regexes)).sort(ascending("number"))
				.into(new ArrayList<Document>());
		cas.add(new Document("count", (int) caCollection.count(and(regexes))));
		return cas;
	}

	public static int count() {
		return (int) caCollection.count();
	}

	@SuppressWarnings("unused")
	public static void createEquipmentCollection() {
		AggregateIterable<Document> output = caCollection.aggregate(Arrays
				.asList(new Document("$group", new Document("_id", "$equipment")), new Document("$out", "equipment")));
		for (Document doc : output) {
		}
	}

	public static boolean delete(String id) {
		// TODO: do not remove file yet. Only do it after we have the historic
		// of every CA
		// FileTools.deleteFile(CAConstants.CA_DIR + caCollection.find(new
		// Document("_id", new ObjectId(id)))
		// .projection(fields(include("fileName"),
		// excludeId())).first().getString("fileName"));
		return caCollection.updateOne(eq("_id", new ObjectId(id)), set("removed", true)).wasAcknowledged();
	}
}
