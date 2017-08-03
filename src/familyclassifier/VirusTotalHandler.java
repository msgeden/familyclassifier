package familyclassifier;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import virustotalapi.ReportScan;
import virustotalapi.VirusTotal;
/**
*
* @author msg
*/
public class VirusTotalHandler {
	public static void retrieveReports(String listFilePath, String downloadFolderPath) {
		try {

			File checksumsListFile = new File(listFilePath);
			String[] shaList = FileHandler.readFileToString(listFilePath).split("\n");
			// int count = 0;
			String parameters = "?apikey=" + FileHandler.readConfigValue(Constants.VIRUS_TOTAL_TOKEN_CONFIG)
					+ "&resource=";

			for (String line : shaList) {
				String[] tabs = line.split("\t");
				String fileName = "";
				String chksum = "";
				if (tabs.length == 1) {
					fileName = line.split("\t")[0];
					chksum = line.split("\t")[0];
				} else {
					fileName = line.split("\t")[0];
					chksum = line.split("\t")[1];
				}
				File jsonFileToSave = new File((downloadFolderPath + fileName.replace(".exe", "") + ".json"));
				File mergedFile = new File(downloadFolderPath + checksumsListFile.getName().replaceAll("tsv", "json"));

				if (!jsonFileToSave.exists()) {
					try {
						String urlStr = parameters + chksum;
						// For public api request limit
						// if ((count) != 0 && count % 4 == 0)
						// Thread.sleep(61000);
						// count++;
						URL url = new URL(Constants.VT_URL_RETRIEVE_REPORTS_API + urlStr);

						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						conn.setRequestProperty("Accept-Encoding", "applcation/json");
						conn.setRequestProperty("User-Agent", "default");
						int responseCode = conn.getResponseCode();
						System.out.println("Trying to Download URL:" + url.toString() + "\nResponse: "
								+ ((responseCode == HttpStatus.SC_OK) ? "Success" : "Failed"));

						if (responseCode == HttpStatus.SC_OK) {
							InputStream in = conn.getInputStream();
							String encoding = conn.getContentEncoding();
							encoding = encoding == null ? "UTF-8" : encoding;
							String body = IOUtils.toString(in, encoding);
							FileUtils.write(jsonFileToSave, body, Charset.defaultCharset(), true);
							FileUtils.write(mergedFile, "\n" + body, Charset.defaultCharset(), true);

							System.out.println(body);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void fileSearch(String query, String listPath, String order, String direction) {
		try {

			String urlStr = "?apikey=" + FileHandler.readConfigValue(Constants.VIRUS_TOTAL_TOKEN_CONFIG) + "&query="
					+ URLEncoder.encode(query, "UTF-8") + "&order=" + order + "&direction=" + direction;

			File listFileToSave = new File(listPath);
			URL url = new URL(Constants.VT_URL_FILE_SEARCH_API + urlStr);
			// FileUtils.deleteQuietly(listFileToSave);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			// conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			// conn.setRequestProperty("Content-Type",
			// "application/x-www-form-urlencoded");
			// conn.setRequestProperty("User-Agent", "gzip, My Python requests
			// library example client or username");

			int responseCode = conn.getResponseCode();
			System.out.println("Trying to Search Query:" + url.toString() + "\nResponse: "
					+ ((responseCode == HttpStatus.SC_OK) ? "Success" : "Failed"));

			if (responseCode == HttpStatus.SC_OK) {
				InputStream in = conn.getInputStream();
				String encoding = conn.getContentEncoding();
				encoding = encoding == null ? "UTF-8" : encoding;
				String body = IOUtils.toString(in, encoding);

				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(body);

				JSONArray hashes = (JSONArray) json.get("hashes");
				if (hashes != null) {
					Iterator<String> iterator1 = hashes.iterator();
					while (iterator1.hasNext()) {
						String hash = (String) iterator1.next();
						FileUtils.write(listFileToSave, hash + "\n", Charset.defaultCharset(), true);

					}

				}

			}

		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void downloadFiles(String listFilePath, String downloadFolderPath) {
		try {

			String[] checksumList = FileHandler.readFileToString(listFilePath).split("\n");
			// int count = 0;
			String parameters = "?apikey=" + FileHandler.readConfigValue(Constants.VIRUS_TOTAL_TOKEN_CONFIG) + "&hash=";

			for (String hash : checksumList) {
				File binFileToSave = new File(downloadFolderPath + File.separator + hash);

				try {
					String urlStr = parameters + hash;
					URL url = new URL(Constants.VT_URL_DOWNLOAD_HASHES_API + urlStr);

					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					// conn.setRequestProperty("Accept-Encoding",
					// "applcation/data");
					// conn.setRequestProperty("User-Agent", "default");

					int responseCode = conn.getResponseCode();
					System.out.println("Trying to Download File:" + url.toString() + "\nResponse: "
							+ ((responseCode == HttpStatus.SC_OK) ? "Success" : "Failed"));
					InputStream input = conn.getInputStream();
					byte[] buffer = new byte[4096];
					int n;

					OutputStream output = new FileOutputStream(binFileToSave);
					while ((n = input.read(buffer)) != -1) {
						output.write(buffer, 0, n);
					}
					output.close();

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void downloadFilesViaLabels(String listFilePath, String downloadFolderPath, String labels) {
		try {

			String[] checksumList = FileHandler.readFileToString(listFilePath).split("\n");
			// int count = 0;
			String parameters = "?apikey=" + FileHandler.readConfigValue(Constants.VIRUS_TOTAL_TOKEN_CONFIG) + "&hash=";
			List<String> labelList = Arrays.asList(labels.split(","));
			for (String line : checksumList) {

				String[] segments = line.split("\t");
				String hash = segments[0];
				String label = segments[1];
				if (labelList.contains(label)) {
					File binFileToSave = new File(downloadFolderPath + File.separator + hash+ ".file");

					String urlStr = parameters + hash;
					URL url = new URL(Constants.VT_URL_DOWNLOAD_HASHES_API + urlStr);

					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					// conn.setRequestProperty("Accept-Encoding",
					// "applcation/data");
					// conn.setRequestProperty("User-Agent", "default");

					int responseCode = conn.getResponseCode();
					System.out.println("Trying to Download File:" + url.toString() + "\nResponse: "
							+ ((responseCode == HttpStatus.SC_OK) ? "Success" : "Failed"));
					InputStream input = conn.getInputStream();
					byte[] buffer = new byte[4096];
					int n;

					OutputStream output = new FileOutputStream(binFileToSave);
					while ((n = input.read(buffer)) != -1) {
						output.write(buffer, 0, n);
					}
					output.close();

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void retrieveDetectionRatio(String shaListFilePath) {
		// TODO Auto-generated method stub

		try {

			String[] shaList = FileHandler.readFileToString(shaListFilePath).split("\n");
			VirusTotal VT = new VirusTotal(FileHandler.readConfigValue(Constants.VIRUS_TOTAL_TOKEN_CONFIG));
			int count = 0;

			for (String line : shaList) {
				int detected = 0;
				int undetected = 0;
				String fileName = line.split("\t")[0];
				String sha256Hex = line.split("\t")[1];
				File reportFile = new File((FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG) + fileName)
						.replaceAll("file", "tsv"));

				Set<ReportScan> reports = VT.ReportScan(sha256Hex);
				String reportcode = VT.getResponseCodeReport(sha256Hex);
				System.out.println("Report Code: " + reportcode + "\n" + VT.getUrlReport(sha256Hex));
				System.out.print("AV" + Constants.TAB_CHAR + "Detected" + Constants.TAB_CHAR + "Malware"
						+ Constants.TAB_CHAR + "Update");
				FileUtils.write(reportFile, "AV" + Constants.TAB_CHAR + "Detected" + Constants.TAB_CHAR + "Malware"
						+ Constants.TAB_CHAR + "Update", Charset.defaultCharset(), true);
				if ((count) != 0 && count % 4 == 0)
					Thread.sleep(61000);
				for (ReportScan report : reports) {
					if (report.getDetected().equals("true")) {
						detected++;
						System.out.print("\n" + report.getVendor() + Constants.TAB_CHAR + report.getDetected()
								+ Constants.TAB_CHAR + report.getMalwarename() + Constants.TAB_CHAR
								+ report.getUpdate());
						FileUtils.write(reportFile,
								"\n" + report.getVendor() + Constants.TAB_CHAR + report.getDetected()
										+ Constants.TAB_CHAR + report.getMalwarename() + Constants.TAB_CHAR
										+ report.getUpdate(),
								Charset.defaultCharset(), true);

					} else
						undetected++;

				}
				FileUtils.write(reportFile,
						"\nDetection Ratio: "
								+ Float.toString((float) (detected + 1) / (float) (detected + undetected + 1)),
						Charset.defaultCharset(), true);

				count++;

			}
		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	public static void checkFiles(String sourceFolder, String fileExtension) {
		// TODO Auto-generated method stub

		try {
			String reportFolderPath = FileHandler.createDirectory(sourceFolder, "reports");

			Collection<File> files = FileHandler.findFiles(sourceFolder, new String[] { fileExtension });

			String reportPath = reportFolderPath + File.separator + "virustotal_results.tsv";
			File reportFile = new File(reportPath);
			// FileUtils.deleteQuietly(reportFile);
			System.out.print("File Name" + Constants.TAB_CHAR + "SHA256" + Constants.TAB_CHAR + "Detected"
					+ Constants.TAB_CHAR + "Undetected");
			FileUtils.write(reportFile, "File Name" + Constants.TAB_CHAR + "SHA256" + Constants.TAB_CHAR + "Detected"
					+ Constants.TAB_CHAR + "Undetected", Charset.defaultCharset(), true);
			int count = 0;
			VirusTotal VT = new VirusTotal(FileHandler.readConfigValue(Constants.VIRUS_TOTAL_TOKEN_CONFIG));
			for (File file : files) {

				String apkReportFilePath = file.getParentFile().getAbsolutePath() + File.separator + "reports"
						+ File.separator + file.getName().replace("apk", "tsv");
				File apkReportFile = new File(apkReportFilePath);
				if (!apkReportFile.exists()) {

					int detected = 0;
					int undetected = 0;

					byte[] data = FileUtils.readFileToByteArray(file);

					String sha256Hex = DigestUtils.sha256Hex(data);
					try {
						count++;
						Set<ReportScan> reports = VT.ReportScan(sha256Hex);
						FileUtils.write(apkReportFile, "\nAV" + Constants.TAB_CHAR + "Detected" + Constants.TAB_CHAR
								+ "Malware" + Constants.TAB_CHAR + "Update", Charset.defaultCharset(), true);
						// Output the details of each scan result from a vendor
						for (ReportScan report : reports) {
							if (report.getDetected().equals("true"))
								detected++;
							else
								undetected++;

							FileUtils.write(apkReportFile,
									"\n" + report.getVendor() + Constants.TAB_CHAR + report.getDetected()
											+ Constants.TAB_CHAR + report.getMalwarename() + Constants.TAB_CHAR
											+ report.getUpdate(),
									Charset.defaultCharset(), true);
						}
					} catch (Exception e) {
						System.out.print("\n" + file.getName() + Constants.TAB_CHAR + "ERROR");
						FileUtils.write(reportFile, "\n" + file.getName() + Constants.TAB_CHAR + "ERROR",
								Charset.defaultCharset(), true);

					}
					if ((detected + undetected) != 0) {
						System.out.print("\n" + file.getName() + Constants.TAB_CHAR + sha256Hex + Constants.TAB_CHAR
								+ detected + Constants.TAB_CHAR + undetected);
						FileUtils
								.write(reportFile,
										"\n" + file.getName() + Constants.TAB_CHAR + sha256Hex + Constants.TAB_CHAR
												+ detected + Constants.TAB_CHAR + undetected,
										Charset.defaultCharset(), true);
					}
				}
				if (count != 0 && count % 4 == 0)
					Thread.sleep(61000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}