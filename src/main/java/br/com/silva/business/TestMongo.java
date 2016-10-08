package br.com.silva.business;

import static com.mongodb.client.model.Filters.regex;

import java.util.ArrayList;

import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCursor;

import br.com.silva.model.CA;
import br.com.silva.resources.MongoResource;

public class TestMongo {
	private static com.mongodb.client.MongoCollection<Document> caCollection = MongoResource.getCollection("ca", "ca");

	public static void main(String[] args) {

		// caCollection.drop();

		// CA caObject = new CA();

		// caObject.setReports(new ArrayList<Report>());

		// for (int i = 0; i < 10; i++) {
		// caObject.setNumber("" + i);
		// Report report = new Report("cnpj", "nome do lab", "numero do report "
		// + i);
		// List<Report> reports = caObject.getReports();
		// reports.add(report);
		// Document caDoc = CAParser.toDocument(caObject);
		// caCollection.insertOne(caDoc);
		// }

		System.out.println("Find one: ");
		findCA(new Document("equipment", "luva"));

		// Document first = caCollection.find().first();
		// System.out.println(first);

		// caCollection.findOneAndReplace(eq("number", caObject.getNumber()),
		// new Document("reports", caObject.getReports()));

		// System.out.println("Find all with into: ");
		// Bson filter = new Document("x", 0).append("y", new Document("$gt",
		// 10)).append("y", new Document("$lte", 50));
		// Bson filter = and(eq("x", 0), gt("y", 10), lte("y", 50));
		//
		// ArrayList<Document> all = caCollection.find(filter).into(new
		// ArrayList<Document>());
		// for (Document document : all) {
		// System.out.println(document);
		// }

		// System.out.println("Find all with iteration: ");
		// MongoCursor<Document> cursor = caCollection.find().iterator();
		// try {
		// while (cursor.hasNext()) {
		// Document cur = cursor.next();
		// System.out.println(cur);
		//
		// }
		// } finally {
		// cursor.close();
		// }
		//
		// System.out.println("Count: ");
		// long count = caCollection.count();
		// System.out.println(count);
	}

	private static void findCA(Document query) {
		String pattern = ".*" + query.getString("equipment") + ".*";
		ArrayList<Document> list = caCollection.find(regex("equipment", pattern, "i")).into(new ArrayList<Document>());

		ArrayList<Document> list2 = caCollection.find().into(new ArrayList<Document>());
		for (Document document : list) {
			System.out.println(document);
		}

		if (query.containsKey("equipment")) {
			String clausule = (String) query.get("equipment");
			// query.put("equipment", new Document("$regex",));

		}

		MongoCursor<Document> cursor = caCollection.find(query).iterator();

		try {
			while (cursor.hasNext()) {
				Document document = cursor.next();
				// Converting BasicDBObject to a custom Class(CA)
				JsonParser parser = new JsonParser();
				JsonObject obj = parser.parse(document.toJson()).getAsJsonObject();

				CA caObj = (new Gson()).fromJson(obj, CA.class);
				System.out.println(caObj.getApprovedFor() + "****" + caObj.getNumber());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
	}

	private static void updateCA() {

	}

}
