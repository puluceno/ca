package br.com.silva.data;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import br.com.silva.resources.MongoResource;

public class WorkingHoursRepository {
	private static MongoCollection<Document> hoursCollection = MongoResource.getDataBase("ca").getCollection("whours");

	public static List<Document> findAll() {
		return hoursCollection.find().into(new ArrayList<Document>());

	}
}
