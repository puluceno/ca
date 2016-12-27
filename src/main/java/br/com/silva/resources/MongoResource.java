package br.com.silva.resources;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoResource {

	private static volatile MongoClient client;
	private static volatile MongoDatabase db;

	public static synchronized MongoClient getClient() {
		if (client == null)
			client = new MongoClient("localhost", 27017);

		return client;
	}

	public static synchronized MongoDatabase getDataBase(String database) {
		if (db == null) {
			if (client == null)
				getClient();
			db = client.getDatabase(database);
		}
		return db;
	}

}