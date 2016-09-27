package br.com.silva.service;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.Unirest;

public class CNPJService {
	private static String url = "http://receitaws.com.br/v1/cnpj/{cnpj}";

	public static Map<String, String> getCompanyInfo(String CNPJ) throws Exception {
		JSONObject json = Unirest.get(url).routeParam("cnpj", CNPJ).asJson().getBody().getObject();

		Map<String, String> companyInfo = new HashMap<String, String>();

		JSONArray activities = (JSONArray) json.get("atividade_principal");
		JSONObject activity = (JSONObject) activities.get(0);
		companyInfo.put("cnae", (String) activity.get("code"));
		companyInfo.put("cnaeText", (String) activity.get("text"));
		companyInfo.put("street", (String) json.get("logradouro"));
		companyInfo.put("number", (String) json.get("numero"));
		companyInfo.put("complement", (String) json.get("complemento"));
		companyInfo.put("neighborhood", (String) json.get("bairro"));
		companyInfo.put("zipcode", (String) json.get("cep"));
		companyInfo.put("city", (String) json.get("municipio"));
		companyInfo.put("state", (String) json.get("uf"));
		Unirest.shutdown();

		return companyInfo;
	}
}