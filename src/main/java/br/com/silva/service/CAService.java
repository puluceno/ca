package br.com.silva.service;

import static spark.Spark.get;
import static spark.Spark.post;

import java.util.Set;
import java.util.logging.Level;

import org.bson.Document;
import org.pmw.tinylog.Logger;

import br.com.silva.business.FileImporter;
import br.com.silva.business.PDFImporter;
import br.com.silva.data.CARepository;
import br.com.silva.data.ParamsRepository;
import br.com.silva.model.CAParser;
import br.com.silva.resources.CorsFilter;

public class CAService {

	public static void main(String[] args) {
		init();

		get("/ca", (req, res) -> {
			Set<String> queryParams = req.queryParams();
			Document query = new Document();
			queryParams.forEach(param -> query.append(param, req.queryParams(param)));

			return CAParser.toJson(CARepository.findCAList(query));
		});

		get("/params", (req, res) -> {
			return CAParser.toJson(ParamsRepository.findParams());
		});

		post("/fileUrl", (req, res) -> {
			String url = req.body();
			if (url != null && !url.isEmpty())
				ParamsRepository.updateParams(url);
			else
				Logger.error("File url is not valid: {}", url);

			return CAParser.toJson(ParamsRepository.findParams());
		});

		post("/updateDatabase", (req, res) -> {
			FileImporter.importCAFile();
			return CAParser.toJson(ParamsRepository.findParams());
		});

	}

	private static void init() {
		clearLogs();
		CorsFilter.apply();
		PDFImporter.importAllPDF();
		FileImporter.scheduleImport();

	}

	private static void clearLogs() {
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.SEVERE);
		java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies")
				.setLevel(Level.SEVERE);
		java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
	}

}
