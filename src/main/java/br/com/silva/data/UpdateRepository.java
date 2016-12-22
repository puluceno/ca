package br.com.silva.data;

import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertManyOptions;

import br.com.silva.resources.MongoResource;

public class UpdateRepository {
	private static MongoCollection<Document> updateCollection = MongoResource.getDataBase("ca").getCollection("update");

	public static void insertList(List<Document> numbers) {
		updateCollection.insertMany(numbers, new InsertManyOptions().ordered(false));
	}

	public static int count() {
		return (int) updateCollection.count();
	}
}
