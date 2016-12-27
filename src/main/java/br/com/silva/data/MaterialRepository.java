package br.com.silva.data;

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
		return materialCollection.find().sort(ascending("name")).into(new ArrayList<Document>());
	}

}
