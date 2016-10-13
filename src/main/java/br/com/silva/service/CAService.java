package br.com.silva.service;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.regex;
import static spark.Spark.get;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.pmw.tinylog.Logger;

import com.mongodb.client.MongoCollection;

import br.com.silva.Tools.CAParser;
import br.com.silva.Tools.PDFGenerator;
import br.com.silva.business.FileImporter;
import br.com.silva.resources.CorsFilter;
import br.com.silva.resources.MongoResource;

public class CAService {
	private static MongoCollection<Document> caCollection = MongoResource.getCollection("ca", "ca");

	public static void main(String[] args) {
		CorsFilter.apply();
		FileImporter.scheduleImport();

		get("/ca", (req, res) -> {
			Set<String> queryParams = req.queryParams();
			Document query = new Document();
			queryParams.forEach(param -> query.append(param, req.queryParams(param)));

			return CAParser.toJson(findCAList(query));
		});

		get("/ca/pdf", (req, res) -> {
			Document ca = findCA(new Document("_id", new ObjectId(req.queryParams("id"))));
			try {
				PDFGenerator.getPDF(res, ca);
				return res;
			} catch (Exception e) {
				Logger.trace(e);
				res.body(e.getMessage());
			}
			return "Erro no servidor!";

		});

	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	public static Document findCA(Document query) {
		return caCollection.find(query).first();
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	public static ArrayList<Document> findCAList(Document query) {
		if (query.isEmpty())
			return caCollection.find().into(new ArrayList<Document>());
		List<Bson> regexes = new ArrayList<Bson>();

		query.keySet().forEach(key -> {
			query.put(key, ".*" + query.getString(key) + ".*");
			regexes.add(regex(key, query.getString(key), "i"));
		});

		return caCollection.find(and(regexes)).into(new ArrayList<Document>());
	}

}
