package br.com.silva.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.servlet.ServletException;

import org.pmw.tinylog.Logger;

import com.github.junrar.Archive;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;

import spark.Request;

public class FileTools {
	/**
	 * Unzips/unrar the source file and puts the extracted file in the given
	 * path with the given file name
	 * 
	 * @param sourceFile
	 * @param destinationFile
	 */
	public static void unzipFile(String sourceFile, String destinationFile) {
		Logger.info("Unzipping file {} to {}", sourceFile, destinationFile);
		File f = new File(sourceFile);
		Archive a = null;
		try {
			a = new Archive(new FileVolumeManager(f));
		} catch (Exception e) {
			Logger.trace(e);
		}
		if (a != null) {
			FileHeader fh = a.nextFileHeader();
			if (fh != null) {
				try {
					File out = new File(destinationFile);
					FileOutputStream os = new FileOutputStream(out);
					a.extractFile(fh, os);
					os.close();
					Logger.info("File unzipped sucesfully to {}", destinationFile);
				} catch (Exception e) {
					Logger.trace(e);
				}
			}
		}
	}

	/**
	 * Downloads a file from a given URL and places it in the given path with
	 * the given file name
	 * 
	 * @param url
	 * @param filePathAndName
	 * @throws IOException
	 */
	public static void downloadFile(URL url, String filePathAndName) throws IOException {
		Logger.info("Downloading file from {}", url);
		InputStream in = url.openStream();
		FileOutputStream fos = new FileOutputStream(new File(filePathAndName));

		int length = -1;
		byte[] buffer = new byte[1024];
		while ((length = in.read(buffer)) > -1) {
			fos.write(buffer, 0, length);
		}

		fos.close();
		in.close();
		Logger.info("File downloaded from {} and saved in {}", url, filePathAndName);
	}

	/**
	 * Saves an uploaded file to the hard drive.
	 * 
	 * @param req
	 * @return true if success.
	 */
	public static boolean saveUploadedFile(Request req, String fileName, String uploadDirectory) {
		File uploadDir = new File(uploadDirectory);
		if (!uploadDir.exists())
			uploadDir.mkdir();
		Path tempFile = null;
		try {
			tempFile = Files.createTempFile(uploadDir.toPath(), "", "");
		} catch (IOException e) {
			Logger.trace(e);
			return false;
		}

		try (InputStream input = req.raw().getPart("file").getInputStream()) {
			Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException | ServletException e) {
			Logger.trace(e);
			return false;
		}
		return new File(tempFile.toString()).renameTo(new File(uploadDirectory + fileName + ".pdf"));
	}

	public static boolean deleteFile(String fullFilePath) {
		return new File(fullFilePath).delete();
	}
}
