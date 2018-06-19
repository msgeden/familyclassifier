/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package familyclassifier;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.util.Collection;

/**
 *
 * @author msgeden
 */
public class FileHandler {

	private static final String configFile = "config.properties";

	public static void deleteUnmatchedFiles(String fileFolderPath) {

		try {
			Collection<File> files = FileUtils.listFiles(FileUtils.getFile(fileFolderPath), TrueFileFilter.INSTANCE,
					TrueFileFilter.INSTANCE);

			for (File file : files) {
				File jsonFile = new File (file.getAbsolutePath().replaceAll("file", "json"));
				if (!jsonFile.exists())
					FileUtils.deleteQuietly(file);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void zipFileWithPasswords(String folderPath, String password, boolean deleteOriginalFile) {

		try {
			Collection<File> files = FileUtils.listFiles(FileUtils.getFile(folderPath), TrueFileFilter.INSTANCE,
					TrueFileFilter.INSTANCE);

			for (File file : files) {

				String output = "";
				ProcessBuilder pb1 = new ProcessBuilder("zip", "--password", password,
						file.getName().replace(".file", ".zip"), file.getName());
				pb1.directory(file.getParentFile());
				try {
					Process p1 = pb1.start();
					output = IOUtils.toString(p1.getInputStream(), Charset.defaultCharset());
					if (output.contains("deflated") && deleteOriginalFile)
						FileUtils.deleteQuietly(file);
					System.out.println(output);
					p1.waitFor(Constants.PROCESS_TIMEOUT_SECS, TimeUnit.SECONDS);
					p1.destroy();
					// file.delete();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	public static void renameFileExtensionsBasedOnHeaders(String folderPath, String filter, String fileExtension) {

		try {
			Collection<File> files = FileUtils.listFiles(FileUtils.getFile(folderPath), TrueFileFilter.INSTANCE,
					FalseFileFilter.INSTANCE);

			for (File file : files) {
				String output = "";

				ProcessBuilder pb1 = new ProcessBuilder("file", file.getAbsolutePath());
				pb1.directory(new File(folderPath));
				try {
					Process p1 = pb1.start();
					output = IOUtils.toString(p1.getInputStream(), Charset.defaultCharset());
					p1.waitFor(Constants.PROCESS_TIMEOUT_SECS, TimeUnit.SECONDS);
					p1.destroy();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (output.contains(filter))
					renameFileExtension(file, fileExtension);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeSHA256CheckumsToFile(String folderPath, String extension) {

		try {
			String resultFilePath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG) + "checksums.tsv";
			File results = new File(resultFilePath);

			System.out.print("File Name" + Constants.TAB_CHAR + "SHA256" + Constants.NEW_LINE);
			FileUtils.write(results, "File Name" + Constants.TAB_CHAR + "SHA256" + Constants.NEW_LINE,
					Charset.defaultCharset(), true);

			Collection<File> files = FileHandler.findFiles(folderPath, new String[] { extension });
			for (File file : files) {
				byte[] data = FileUtils.readFileToByteArray(file);
				String sha256 = DigestUtils.sha256Hex(data);
				System.out.print(file.getName() + Constants.TAB_CHAR + sha256 + Constants.NEW_LINE);
				FileUtils.write(results, file.getName() + Constants.TAB_CHAR + sha256 + Constants.NEW_LINE,
						Charset.defaultCharset(), true);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeCheckumsToFile(String folderPath, String extension, String digestType) {

		try {

			String resultFilePath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG) + digestType
					+ "_checksums.tsv";
			File results = new File(resultFilePath);

			System.out.print("File Name" + Constants.TAB_CHAR + digestType.toUpperCase() + Constants.NEW_LINE);
			FileUtils.write(results, "File Name" + Constants.TAB_CHAR + digestType.toUpperCase() + Constants.NEW_LINE,
					Charset.defaultCharset(), true);
			Collection<File> files = FileHandler.findFiles(folderPath, new String[] { extension });
			for (File file : files) {
				byte[] data = FileUtils.readFileToByteArray(file);
				String checksum = "";
				if (digestType.toUpperCase().contains("MD5"))
					checksum = DigestUtils.md5Hex(data);
				else if (digestType.toUpperCase().contains("SHA1"))
					checksum = DigestUtils.shaHex(data);
				else if (digestType.toUpperCase().contains("SHA256"))
					checksum = DigestUtils.sha256Hex(data);
				else
					System.out.print("Invalid Digest Type!");

				System.out.print(file.getName() + Constants.TAB_CHAR + checksum + Constants.NEW_LINE);
				FileUtils.write(results, file.getName() + Constants.TAB_CHAR + checksum + Constants.NEW_LINE,
						Charset.defaultCharset(), true);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getSampleNameOfFile(String appPath, String extension) {
		// Retrieve sample name from file path
		if (appPath.contains(Constants.UNDERSCORE + extension.toUpperCase())) {
			String[] folders = appPath.substring(0, appPath.lastIndexOf(Constants.UNDERSCORE + extension.toUpperCase()))
					.split(File.separator);
			return folders[folders.length - 1];
		} else {
			String[] folders = appPath.split(File.separator);
			return folders[folders.length - 1];
		}
	}

	public static String readConfigValue(String key) {
		Properties prop = new Properties();
		InputStream input;
		try {
			input = new FileInputStream(configFile);
			prop.load(input);
		} catch (IOException e) {
			System.out.println("Cannot read configuration file(s)\n" + e.getMessage());
		}
		return prop.getProperty(key);
	}

	public static void writeConfigValue(String key, String val) {
		Properties prop = new Properties();
		InputStream input;
		OutputStream output;
		try {
			input = new FileInputStream(configFile);
			prop.load(input);
		} catch (IOException e) {
			System.out.println("Cannot read configuration file(s)\n" + e.getMessage());
		}
		prop.setProperty(key, val);
		try {
			output = new FileOutputStream(configFile);
			prop.store(output, "");
		} catch (IOException e) {
			System.out.println("Cannot read configuration file(s)\n" + e.getMessage());
		}
	}

	public static String createDirectory(String path, String dir) {
		File directory = new File(path + dir);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return directory.getAbsolutePath();
	}

	public static void moveFilesFromList(String sourcePath, String destinationPath, String listFile)
			throws IOException {

		File destDir = new File(destinationPath);
		File tsvFile = new File(listFile);
		String[] fileList = FileUtils.readFileToString(tsvFile,Charset.defaultCharset()).split("\n");
		for (String s : fileList) {
			try {
				String name = s.split("\t")[0];
				FileUtils.moveFileToDirectory(new File(sourcePath + File.separator + name), destDir, true);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(s);
			}
		}
	}

	public static void copyFilesFromList(String listFilePath) throws IOException {

		String[] fileList = FileHandler.readFileToString(listFilePath).split("\n");
		for (String s : fileList) {
			try {
				String source = s.split("\t")[3];
				String dest = s.split("\t")[4];
				FileUtils.copyFile(new File(source), new File(dest), true);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(s);
			}
		}
	}

	public static void copyFilesFromJsons(String folder) throws IOException {

		ArrayList<File> jsonList = findFilesAsArray(folder, new String[]{"json"});
		for (File file : jsonList) {
			String s=file.getAbsolutePath();
			String dest="";
			String source="";
			try {
				dest=s.replaceAll("json", "file");
				source=s.replaceAll("ransomware-small", "ransomware").replaceAll("json","file");
				if (s.contains("train"))
					source=source.replaceAll("train", "test");
				FileUtils.copyFile(new File(source), new File(dest), true);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(s);
			}
		}
	}
	
	public static void moveFilesRandomly(String sourcePath, String destinationPath, int destCount, String fileExtension)
			throws IOException {
		File destDir = new File(destinationPath);
		Collection<File> files = FileHandler.findFiles(sourcePath, new String[] { fileExtension });

		ArrayList<File> apklist = new ArrayList<File>(files);
		int size = apklist.size();
		int randIndex = 0;
		HashSet<String> list = new HashSet<String>();
		while (list.size() < destCount) {
			randIndex = randWithinRange(0, size - 1);
			list.add(apklist.get(randIndex).getPath());
		}
		for (String s : list) {
			FileUtils.moveFileToDirectory(new File(s), destDir, true);
		}
	}

	public static void renameFilesFromList(String sourcePath, String listFile) throws IOException {

		String[] fileList = FileHandler.readFileToString(listFile).split("\r");
		for (String s : fileList) {
			String name = s.split("\t")[0];
			String prefix = s.split("\t")[1].replace(".", Constants.UNDERSCORE);
			File file = new File(sourcePath + name);
			String newName = file.getParent() + File.separator + prefix + Constants.UNDERSCORE + name;
			file.renameTo(new File(newName));
		}
	}

	public static void renameFileExtension(File file, String newExtension) throws IOException {

		String absolutePath = file.getAbsolutePath();
		String newfilePath = absolutePath + "." + newExtension;
		file.renameTo(new File(newfilePath));
	}

	public static void renameFileExtensionsInAFolder(String folderPath, String newExtension) throws IOException {
		File folder = new File(folderPath);
		Collection<File> files = FileUtils.listFiles(folder, null, false);
		for (File file : files) {
			renameFileExtension(file, newExtension);
		}
	}

	public static void renameFilesWithMd5(String sourcePath, String fileExtension) throws IOException {
		Collection<File> files = findFiles(sourcePath, null);

		for (File file : files) {
			FileInputStream fis = new FileInputStream(file);
			String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			fis.close();
			md5 += "." + fileExtension;
			md5 = file.getAbsolutePath().replace(file.getName(), md5);
			file.renameTo(new File(md5));
		}

	}

	public static void copyFilesRandomly(String sourcePath, String destinationPath, int destCount, String fileExtension)
			throws IOException {
		File destDir = new File(destinationPath);
		Collection<File> files = FileHandler.findFiles(sourcePath, new String[] { fileExtension });

		ArrayList<File> apklist = new ArrayList<File>(files);
		int size = apklist.size();
		int randIndex = 0;
		HashSet<String> list = new HashSet<String>();
		while (list.size() < destCount) {
			randIndex = randWithinRange(0, size - 1);
			list.add(apklist.get(randIndex).getPath());
		}
		for (String s : list) {
			FileUtils.copyFileToDirectory(new File(s), destDir, true);
		}
	}

	public static void moveFileToDirectory(String filePath, String destinationPath) throws IOException {
		FileUtils.moveFileToDirectory(new File(filePath), new File(destinationPath), true);
	}

	public static byte[] readFileToByteArray(String filePath) throws IOException {
		return FileUtils.readFileToByteArray(FileUtils.getFile(filePath));
	}

	public static String readFileToString(String filePath) throws IOException {
		return FileUtils.readFileToString(FileUtils.getFile(filePath), Charset.defaultCharset());
	}

	public static String readFileToHexString(String filePath) throws IOException {
		byte[] fileBytes = FileUtils.readFileToByteArray(FileUtils.getFile(filePath));
		StringBuilder sb = new StringBuilder();
		for (byte b : fileBytes) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}

	public static String[] findFolderContents(String path) {
		File file = new File(path);
		try {
			String[] contents = file.list(new FilenameFilter() {
				@Override
				public boolean accept(File current, String name) {
					return new File(current, name).isDirectory();
				}
			});
			for (int i = 0; i < contents.length; i++) {
				contents[i] = path + contents[i];
			}

			return contents;
		} catch (Exception e) {
			return null;
		}
	}

	public static ArrayList<File> findFilesAsArray(String path, String[] extensions) {
		Collection<File> fileCollection = FileUtils.listFiles(FileUtils.getFile(path), extensions, true);
		List<File> fileList = new ArrayList<File>(fileCollection);
		return (ArrayList<File>) fileList;
	}

	public static Collection<File> findFiles(String path, String[] extensions) {
		return FileUtils.listFiles(FileUtils.getFile(path), extensions, true);
	}

	public static int randWithinRange(int min, int max) {
		Random rand = new Random();
		int numValue = rand.nextInt((max - min) + 1) + min;
		return numValue;
	}
}