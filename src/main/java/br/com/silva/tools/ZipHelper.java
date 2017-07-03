package br.com.silva.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.pmw.tinylog.Logger;

import br.com.silva.model.CAConstants;

public class ZipHelper {
	public static void main(String[] args) throws IOException {
		// System.out.println(zipFile(true,
		// "/home/pulu/Documents/Test/13026_22062009.pdf"));
		// System.out.println(unzip(true,
		// "/home/pulu/Documents/Test/13026_22062009.zip"));
		ZipHelper.zipFolder(false, CAConstants.CA_DIR.substring(0, CAConstants.CA_DIR.length() - 1));

	}

	/**
	 * Zips one or multiple files into their own folder.
	 * 
	 * @param sourceFiles
	 *            The complete file path including the complete file name.
	 * @return True if operation was successful. False otherwise.
	 */
	public static boolean zipFile(boolean deleteSource, String... sourceFiles) {
		for (String sourceFile : sourceFiles) {

			File fileToZip = new File(sourceFile);

			try (FileOutputStream fos = new FileOutputStream(
					sourceFile.substring(0, sourceFile.lastIndexOf(".")) + ".zip");
					ZipOutputStream zipOut = new ZipOutputStream(fos);
					FileInputStream fis = new FileInputStream(fileToZip)) {

				ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
				zipOut.putNextEntry(zipEntry);
				final byte[] bytes = new byte[1024];
				int length;
				while ((length = fis.read(bytes)) >= 0) {
					zipOut.write(bytes, 0, length);
				}

				if (deleteSource)
					fileToZip.delete();
			} catch (Exception e) {
				Logger.trace(e);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param folderPath
	 * @return
	 */
	public static boolean zipFilesInFolder(boolean deleteSource, String folderPath) {
		File folder = new File(folderPath);
		if (!folder.isDirectory())
			return false;

		for (File sourceFile : folder.listFiles()) {

			try (FileOutputStream fos = new FileOutputStream(sourceFile + ".zip");
					ZipOutputStream zipOut = new ZipOutputStream(fos);
					FileInputStream fis = new FileInputStream(sourceFile)) {

				ZipEntry zipEntry = new ZipEntry(sourceFile.getName());
				zipOut.putNextEntry(zipEntry);
				final byte[] bytes = new byte[1024];
				int length;
				while ((length = fis.read(bytes)) >= 0) {
					zipOut.write(bytes, 0, length);
				}
				if (deleteSource)
					folder.delete();
			} catch (Exception e) {
				Logger.trace(e);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param folder
	 * @return
	 */
	public static boolean zipFolder(boolean deleteSource, String folder) {
		try (FileOutputStream fos = new FileOutputStream(getPathAndFilename(folder));
				ZipOutputStream zipOut = new ZipOutputStream(fos);) {
			File fileToZip = new File(folder);

			zipFile(fileToZip, fileToZip.getName(), zipOut);
			if (deleteSource)
				fileToZip.delete();
			return true;
		} catch (Exception e) {
			Logger.trace(e);
			return false;
		}
	}

	/**
	 * 
	 * @param pathFileName
	 * @return
	 */
	public static boolean unzip(boolean deleteSource, String pathFileName) {
		byte[] buffer = new byte[1024];
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(pathFileName));) {
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				String fileName = zipEntry.getName();
				File newFile = new File(getPath(pathFileName) + fileName);
				checkDirExists(newFile);
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();

			if (deleteSource)
				new File(pathFileName).delete();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.trace(e);
			return false;
		}
	}

	private static void checkDirExists(File newFile) {
		new File(newFile.getAbsolutePath().substring(0, newFile.getAbsolutePath().lastIndexOf(File.separator))).mkdir();

	}

	private static String getPathAndFilename(String folder) {
		return folder.substring(0, folder.lastIndexOf(File.separator))
				+ folder.substring(folder.lastIndexOf(File.separator, folder.length())) + ".zip";
	}

	private static String getPath(String pathFileName) {
		return pathFileName.substring(0, pathFileName.lastIndexOf(File.separator)) + File.separator;
	}

	private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}

}