package br.com.silva.service;

import static spark.Spark.get;

import java.util.ArrayList;
import java.util.Set;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import br.com.silva.Tools.CAParser;
import br.com.silva.resources.CorsFilter;
import br.com.silva.resources.MongoResource;

public class CAService {
	private static MongoCollection<Document> caCollection = MongoResource.getCollection("ca", "ca");

	public static void main(String[] args) {
		CorsFilter.apply();

		get("/ca/:number", (req, res) -> {
			return CAParser.toJson(findCA(new Document("number", req.params(":number"))));
		});

		get("/ca", (req, res) -> {
			Set<String> queryParams = req.queryParams();
			Document query = new Document();
			queryParams.forEach(param -> query.append(param, req.queryParams(param)));

			return CAParser.toJson(findCAList(query));
		});
	}

	public static Document findCA(Document query) {
		return caCollection.find(query).first();
	}

	public static ArrayList<Document> findCAList(Document query) {
		return caCollection.find(query).into(new ArrayList<Document>());
	}

}
