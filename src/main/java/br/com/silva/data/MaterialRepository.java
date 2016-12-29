package br.com.silva.data;

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Sorts.ascending;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import br.com.silva.resources.MongoResource;

public class MaterialRepository {
	private static MongoCollection<Document> materialCollection = MongoResource.getDataBase("ca")
			.getCollection("material");

	public static List<Document> findAll() {
		return materialCollection.find().projection(excludeId()).sort(ascending("text"))
				.into(new ArrayList<Document>());
	}

	public static void checkAndInsert(String materialText) {
		Document query = new Document("text", materialText.toLowerCase());
		if (materialCollection.find(query).first() == null)
			materialCollection.insertOne(query);
	}

}
