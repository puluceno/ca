package br.com.silva.data;

import java.util.Date;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import br.com.silva.resources.MongoResource;

public class AnalysisRepository {
	private static MongoCollection<Document> analysisCollection = MongoResource.getDataBase("ca")
			.getCollection("analysis");

	public static void save(Document analysis) {
		if (analysis.getString("name") == null || analysis.getString("name").isEmpty()) {
			// concatenate agent names with equipment names and date at the end.
		}
		analysis.append("created", new Date());
		analysisCollection.insertOne(analysis);
	}
}
