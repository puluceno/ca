package br.com.silva.data;

import static com.mongodb.client.model.Sorts.ascending;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import br.com.silva.resources.MongoResource;

public class ProfileRepository {
	private static MongoCollection<Document> profileCollection = MongoResource.getDataBase("ca")
			.getCollection("profile");

	public static List<Document> findAll() {
		return profileCollection.find().sort(ascending("profile")).into(new ArrayList<Document>());
	}
}
