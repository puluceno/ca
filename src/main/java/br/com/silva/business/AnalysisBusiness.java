package br.com.silva.business;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import br.com.silva.data.CARepository;
import br.com.silva.model.Messages;
import br.com.silva.tools.TimeTools;
import spark.Request;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AnalysisBusiness {

	public static Messages verifyAnalysis(Request req) {
		try (BufferedReader agentsReader = new BufferedReader(
				new InputStreamReader(req.raw().getPart("agents").getInputStream(), StandardCharsets.UTF_8));
				BufferedReader deliveriesReader = new BufferedReader(new InputStreamReader(
						req.raw().getPart("deliveries").getInputStream(), StandardCharsets.UTF_8))) {

			List<LinkedTreeMap> agents = new Gson().fromJson(agentsReader, List.class);
			List<LinkedTreeMap> deliveries = new Gson().fromJson(deliveriesReader, List.class);

			if (!isCAValid(agents, deliveries)) {
				return Messages.CA_EXPIRED_WHEN_DELIVERED;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static boolean isCAValid(List<LinkedTreeMap> agents, List<LinkedTreeMap> deliveries) {
		for (LinkedTreeMap delivery : deliveries) {
			delivery.put("deliveryDate", TimeTools.convertDate((String) delivery.get("deliveryDate")));
			Document ca = CARepository.findCA(new Document("number", String.valueOf(delivery.get("ca"))));

			if (!TimeTools.compareStringDates(ca.getString("date"), (String) delivery.get("deliveryDate")))
				return false;
		}
		return true;
	}
}
