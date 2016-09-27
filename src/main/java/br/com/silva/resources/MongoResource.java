package br.com.silva.resources;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoResource {

	private static MongoClient client;
	private static MongoDatabase db;
	private static MongoCollection<Document> collection;

	public static MongoClient getClient() {
		if (client == null)
			client = new MongoClient("localhost", 27017);

		return client;
	}

	public static MongoDatabase getDataBase(String database) {
		if (db == null) {
			if (client == null)
				getClient();
			db = client.getDatabase(database);
		}
		return db;
	}

	public static MongoCollection<Document> getCollection(String collectionName, String database) {
		if (collection == null) {
			if (db == null)
				getDataBase(database);
			collection = db.getCollection(collectionName);
		}
		return collection;
	}

}