package br.com.silva.data;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import br.com.silva.resources.MongoResource;

public class LoginRepository {
	private static MongoCollection<Document> loginCollection = MongoResource.getDataBase("ca").getCollection("login");

	public static List<Document> find(Document query) {
		return loginCollection.find(query).into(new ArrayList<Document>());
	}

	public static void createSession(Document session) {
		loginCollection.insertOne(session);
	}

	public static void clearUserSessions(Document user) {
		loginCollection.deleteMany(eq("login", user.getString("login")));
	}
}
