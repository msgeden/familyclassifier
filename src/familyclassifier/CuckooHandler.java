/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package familyclassifier;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
*
* @author msgeden
*/
public class CuckooHandler {

	public static void startCuckooSandbox() {

		String cuckooPath = FileHandler.readConfigValue(Constants.CUCKOO_PATH_CONFIG);

		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.directory(new File(cuckooPath));
			pb.command("./cuckoo.py");
			pb.start();
			pb.command("./utils/api.py");
			pb.start();
			pb.directory(new File(cuckooPath + "/web/"));
			pb.command("./manage.py runserver");
			pb.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void cleanCuckooSandbox() {

		String cuckooPath = FileHandler.readConfigValue(Constants.CUCKOO_PATH_CONFIG);

		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.directory(new File(cuckooPath));
			pb.command("./cuckoo.py --clean");
			pb.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void submitFile(String filePath, String fileExtension, String pwd, int timeout) {

		File analysisFoldersInfo = new File(
				FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG) + "cuckoo-analysis-folder-mapping.tsv");
		File file = new File(filePath);

		String cuckooPath = FileHandler.readConfigValue(Constants.CUCKOO_PATH_CONFIG);
		try {
			ProcessBuilder pb = new ProcessBuilder("curl", "-F", "file=@" + filePath,
					(timeout != -1) ? ("-F timeout=" + timeout) : "",
					(fileExtension != null) ? ("-F" + " package=" + fileExtension) : "",
					(pwd != null && !pwd.equals("")) ? ("-F " + "options=\"password=" + pwd + "\"") : "",
					Constants.CUCKOO_CREATE_FILE_API);
			pb.directory(new File(cuckooPath));
			Process p1 = pb.start();
			Thread.sleep(3000);
			String response = IOUtils.toString(p1.getInputStream(), Charset.defaultCharset());

			long id = (long) ((JSONObject) (new JSONParser()).parse(response)).get("task_id");
			System.out.println(
					file.getName() + " from family " + file.getParentFile().getName() + " added with a task id " + id);

			String cuckooJsonReportStr = FileHandler.readConfigValue(Constants.CUCKOO_PATH_CONFIG)
					+ "/storage/analyses/" + id + "/reports/report.json";
			String copyOfJsonReportStr = file.getParent() + File.separator + "json" + File.separator
					+ file.getName().replace("." + fileExtension, ".json");

			FileUtils.write(analysisFoldersInfo,
					id + Constants.TAB_CHAR + file.getName() + Constants.TAB_CHAR + file.getParentFile().getName()
							+ Constants.TAB_CHAR + cuckooJsonReportStr + Constants.TAB_CHAR + copyOfJsonReportStr
							+ Constants.NEW_LINE,
					Charset.defaultCharset(), true);

			Thread.sleep(timeout * 1000 + 40000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void submitAllFiles(String folderPath, String fileExtension, String pwd, int timeout) {
		Collection<File> files = FileUtils.listFiles(new File(folderPath), new String[] { fileExtension }, true);
		for (File file : files) {
			submitFile(file.getAbsolutePath(), fileExtension, pwd, timeout);
		}
	}
}