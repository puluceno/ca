package br.com.silva.business;

import org.bson.Document;

import br.com.silva.data.AgentRepository;
import br.com.silva.model.JsonTransformer;
import spark.Request;

public class AgentBusiness {

	public static Object checkAndInsert(Request req) {
		Document agent = Document.parse(req.body().toLowerCase());
		if (agent.getString("text").length() > 1)
			if (AgentRepository.checkAndInsert(agent))
				return JsonTransformer.toJson(AgentRepository.findAll());

		return "";
	}
}
