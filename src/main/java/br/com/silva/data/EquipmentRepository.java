package br.com.silva.data;

import static com.mongodb.client.model.Sorts.ascending;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import br.com.silva.resources.MongoResource;

public class EquipmentRepository {
	private static MongoCollection<Document> equipCollection = MongoResource.getDataBase("ca")
			.getCollection("equipment");

	public static List<Document> findAll() {
		return equipCollection.find().sort(ascending("_id")).into(new ArrayList<Document>());
	}

}
