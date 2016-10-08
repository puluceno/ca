package br.com.silva.service;

import static spark.Spark.get;

import java.util.ArrayList;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;

import br.com.silva.Tools.CAParser;
import br.com.silva.Tools.PDFGenerator;
import br.com.silva.resources.CorsFilter;
import br.com.silva.resources.MongoResource;

public class CAService {
	private static MongoCollection<Document> caCollection = MongoResource.getCollection("ca", "ca");

	public static void main(String[] args) {
		CorsFilter.apply();

		get("/ca", (req, res) -> {
			Set<String> queryParams = req.queryParams();
			Document query = new Document();
			queryParams.forEach(param -> query.append(param, req.queryParams(param)));

			return CAParser.toJson(findCAList(query));
		});

		get("/pdf", (req, res) -> {
			Document ca = findCA(new Document("_id", new ObjectId(req.queryParams("id"))));
			try {
				PDFGenerator.getPDF(res, ca);
			} catch (Exception e) {
				res.body(e.getMessage());
			}
			return res;
		});
	}

	public static Document findCA(Document query) {
		return caCollection.find(query).first();
	}

	public static ArrayList<Document> findCAList(Document query) {
		// List<Bson> regexes = new ArrayList<Bson>();
		//
		// Set<String> keySet = query.keySet();
		// for (String key : keySet) {
		// query.put(key, "*." + query.getString(key) + ".*");
		// regexes.add(regex(key, query.getString(key), "i"));
		// }

		// query.keySet().forEach(key -> {
		// query.put(key, "*." + query.getString(key) + ".*");
		// regexes.add(regex(key, query.getString(key), "i"));
		// });

		// return caCollection.find(and(regexes)).limit(10).into(new
		// ArrayList<Document>());
		return caCollection.find(query).limit(10).into(new ArrayList<Document>());
	}

}
