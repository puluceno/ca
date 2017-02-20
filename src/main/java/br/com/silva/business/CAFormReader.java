package br.com.silva.business;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.mongodb.client.MongoCollection;

import br.com.silva.data.CARepository;
import br.com.silva.model.CAConstants;
import br.com.silva.resources.MongoResource;
import br.com.silva.tools.FileTools;
import br.com.silva.tools.MaskTools;
import spark.Request;

public class CAFormReader {
	private static MongoCollection<Document> caCollection = MongoResource.getDataBase("ca").getCollection("ca");

	@SuppressWarnings("rawtypes")
	public static String readAndSave(Request req) {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(req.raw().getPart("data").getInputStream(), StandardCharsets.UTF_8));
			Document ca = new Gson().fromJson(reader, Document.class);

			String number = String.valueOf(((Double) ca.get("number")).intValue());
			if (!number.matches("^[1-9]([0-9]{1,4}$)"))
				throw new Exception("Número do CA inválido!");

			if (CARepository.findCA(new Document("number", number).append("date", ca.getString("date"))
					.append("approvedFor", ca.getString("approvedFor"))) != null)
				throw new Exception("CA já existente no sistema!");

			if (ca.containsKey("technicalRules")) {
				ArrayList techRules = (ArrayList) ca.get("technicalRules");
				ca.remove("technicalRules");

				if (!((LinkedTreeMap) techRules.get(techRules.size() - 1)).isEmpty()) {
					List<String> technicalRules = new ArrayList<String>();
					for (int i = 0; i < techRules.size(); i++) {
						LinkedTreeMap rules = (LinkedTreeMap) techRules.get(i);
						for (Object key : rules.keySet()) {
							technicalRules.add(rules.get(key).toString());
						}
					}
					ca.append("technicalRules", technicalRules);
				}
			}

			if (ca.containsKey("reports")) {
				ArrayList reports = (ArrayList) ca.get("reports");
				if (((LinkedTreeMap) reports.get(reports.size() - 1)).isEmpty()) {
					ca.remove("reports");
				}
			}

			ca.remove("number");
			ca.append("number", number);

			String fileName = number + "_" + MaskTools.unMaskDate(ca.getString("date"));

			FileTools.saveUploadedFile(req, fileName, CAConstants.CA_DIR);
			ca.remove("file");
			ca.append("fileName", fileName + ".pdf");
			caCollection.insertOne(ca);

			return "CA " + number + " inserido com sucesso!";
		} catch (Exception e) {
			if (e instanceof NullPointerException)
				return "CA não possui dados suficientes!";
			return e.getMessage();
		}

	}
}
