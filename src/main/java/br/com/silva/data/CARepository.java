package br.com.silva.data;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.AggregateIterable;
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
		Boolean exactSearch = Boolean.valueOf(query.getString("exactSearch"));
		if (exactSearch) {
			query.remove("exactSearch");
			ArrayList<Document> exact = caCollection.find(query).limit(100).into(new ArrayList<Document>());
			exact.add(new Document("count", (int) caCollection.count(query)));
			return exact;
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

	@SuppressWarnings("unused")
	public static void createEquipmentCollection() {
		AggregateIterable<Document> output = caCollection.aggregate(Arrays
				.asList(new Document("$group", new Document("_id", "$equipment")), new Document("$out", "equipment")));
		for (Document doc : output) {
		}
	}
}
