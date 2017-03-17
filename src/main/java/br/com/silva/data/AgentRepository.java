package br.com.silva.data;

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Sorts.ascending;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import br.com.silva.resources.MongoResource;

public class AgentRepository {
	private static MongoCollection<Document> agentCollection = MongoResource.getDataBase("ca").getCollection("agent");

	public static List<Document> findAll() {
		return agentCollection.find().projection(excludeId()).sort(ascending("text")).into(new ArrayList<Document>());
	}

	public static boolean checkAndInsert(Document query) {
		if (agentCollection.find(query).first() == null) {
			agentCollection.insertOne(query);
			return true;
		} else {
			return false;
		}
	}
}
