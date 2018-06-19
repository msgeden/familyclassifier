package familyclassifier;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
/**
*
* @author msgeden
*/
public class FamilyClassifier {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Family Classifier Started!");
		BasicParser parser = new BasicParser();
		HelpFormatter formatter = new HelpFormatter();

		Options options = new Options()
				.addOption(new Option("ghash", "-generate-file-hashes", true, "specify the folder of files [f]"))
				.addOption(new Option("hash", "-hash-type", true, "specify the type of hash algorithm  [t]"))
				.addOption(new Option("zip", "-zip-files", true, "specify the folder of files to be zipped [f]"))
				.addOption(new Option("pwd", "-zip-password", true, "specify the password of the zip files [p]"))
				.addOption(new Option("rn", "-rename-extensions", true, "specify the folder of files for renaming [f]"))
				.addOption(new Option("gcuckoo", "-generate-cuckoo-reports", true,
						"specify the folder of files to be submitted [f]"))
				.addOption(new Option("cpy", "-copy-report-files", true, "specify the location of the files list [f]"))
				.addOption(new Option("rnf", "-rename-extensions-of-folder", true,
						"specify the location of the folder [f]"))
				.addOption(new Option("mvf", "-move-files-randomly", true,
						"specify the number of files to be removed [n]"))

				.addOption(new Option("time", "-timeout-sec", true, "specify the seconds for timeout [t]"))

				.addOption(new Option("flt", "-file-filter", true, "specify the filter output of file [f]"))
				.addOption(new Option("ext", "-file-extension", true, "specify the new file extension for rename [e]"))
				.addOption(
						new Option("vtj", "-vt-json-reports", true, "specify the location of checksum list file  [f]"))
				.addOption(new Option("vts", "-vt-search-file", true, "specify the query of file search [q]"))
				.addOption(new Option("vtd", "-vt-download-checksums", true,
						"specify the location of checksum list file  [f]"))
				.addOption(new Option("p", "-folder-path", true, "specify the location of download folder [f]"))
				.addOption(new Option("sp", "-source-path", true, "specify the location of source [p]"))
				.addOption(new Option("dp", "-destination-path", true, "specify the location of destination [p]"))

				.addOption(new Option("tp", "-training-path", true, "specify the folder of reports for training [f]"))
				.addOption(
						new Option("vp", "-validation-path", true, "specify the folder of reports for validation [f]"))
				.addOption(new Option("gid", "-generate-api-ids", false, "generate APICall IDs for pattern searh"))
				.addOption(
						new Option("xf", "-extract-features", false, "extract distinctive features from training set"))
				.addOption(new Option("df", "-distinctive-features", true,
						"specify the location of distinctive features [f]"))
				.addOption(new Option("gwf", "-generate-weka", false,
						"generate weka files from distinctive features by specifying the number of inputs for generation seperated by commas [n]"))

				.addOption(
						new Option("wt", "-weka-training-file", true, "specify the path of weka file for training [f]"))
				.addOption(new Option("wv", "-weka-validation-file", true,
						"specify the path of weka file for validation [f]"))
				.addOption(new Option("cw", "-classify-with-weka", true,
						"classify apps with the given weka algorithm [a]"))
				.addOption(new Option("h", "-help", false, "display list of commands"))

				.addOption(new Option("cwa", "-classify-with-all", true, "classify apps with all weka algorithms"));

		// args = new String[] { 
		// "-vts","engines:\"CryptoLocker\" size:10MB- type:peexe positives:5+
		// fs:2011-01-01T19:59:22+","-p","/home/cuckoo/Desktop/data/ransomware/list.tsv"};
		// args = new String[] {
		// "-vtj","/home/cuckoo/Desktop/data/ransomware/cryptoLocker/sha256.tsv","-p","/home/cuckoo/Desktop/data/ransomware/cryptoLocker/vtreports/"};
		// args = new String[] {
		// "-vtd","/home/cuckoo/Desktop/data/ransomware/cryptoWall/labels/avclass.tsv","-p","/home/cuckoo/Desktop/data/ransomware/cryptoWall/","-flt","cryptowall"
		// };

		// args = new String[] {
		// "-rnf","/home/cuckoo/Desktop/data/ransomware/ransomware/WannaCry/","-ext","file"};
		// args = new String[] {
		// "-mvf","22","-sp","/home/cuckoo/Desktop/data/zmist/file","-dp","/home/cuckoo/Desktop/data/samples/zmist/train/zmist/","-ext","zip"};

		// args = new String[] {
		// "-zip","/home/cuckoo/Desktop/data/ransomware/","-pwd","infected"};

		 //args = new String[] { "-gcuckoo","/home/cuckoo/Desktop/data/zmist/","-ext","zip","-pwd","infected","-time","120" };

		
		//args = new String[] { "-gid" };

		//args = new String[] { "-xf" };
		//args = new String[]{"-gwf"};
		//args = new String[]{"-cwa","apicall2tuple"};
		
	
		try {
			CommandLine commandLine = parser.parse(options, args);
			if (commandLine.hasOption("h")) {
				formatter.printHelp("familyclassifier", options);

			} else if (commandLine.hasOption("ghash")) {
				System.out.println("Generating file digest checksums...");
				FileHandler.writeCheckumsToFile(commandLine.getOptionValue("ghash"), commandLine.getOptionValue("ext"),
						commandLine.getOptionValue("hash"));
				System.out.println("Finished.!");
			} else if (commandLine.hasOption("rn")) {
				System.out.println("Renaming Samples Based on Headers...");
				FileHandler.renameFileExtensionsBasedOnHeaders(commandLine.getOptionValue("rn"),
						commandLine.getOptionValue("flt"), commandLine.getOptionValue("ext"));
				System.out.println("Finished.!");

			} else if (commandLine.hasOption("rnf")) {
				System.out.println("Renaming Files in a Folder...");
				FileHandler.renameFileExtensionsInAFolder(commandLine.getOptionValue("rnf"),
						commandLine.getOptionValue("ext"));
				System.out.println("Finished.!");

			} else if (commandLine.hasOption("mvf")) {
				System.out.println("Moving Files in a Folder Randomly...");
				FileHandler.moveFilesRandomly(commandLine.getOptionValue("sp"), commandLine.getOptionValue("dp"),
						Integer.parseInt(commandLine.getOptionValue("mvf")), commandLine.getOptionValue("ext"));
				System.out.println("Finished.!");
			} else if (commandLine.hasOption("zip")) {
				System.out.println("Zipping Samples with Given Password...");
				FileHandler.zipFileWithPasswords(commandLine.getOptionValue("zip"), commandLine.getOptionValue("pwd"),
						true);
				System.out.println("Finished.!");

			} else if (commandLine.hasOption("vts")) {
				System.out.println("Search VirusTotal query...");
				VirusTotalHandler.fileSearch(commandLine.getOptionValue("vts"), commandLine.getOptionValue("p"), "fs",
						"asd");
				System.out.println("Finished.!");

			} else if (commandLine.hasOption("vtj")) {
				System.out.println("Generating VirusTotal reports...");
				VirusTotalHandler.retrieveReports(commandLine.getOptionValue("vtj"),
						commandLine.getOptionValue("p"));
				System.out.println("Finished.!");

			} else if (commandLine.hasOption("vtd")) {
				System.out.println("Downloading samples from VirusTotal...");
				VirusTotalHandler.downloadFiles(commandLine.getOptionValue("vtd"),
						commandLine.getOptionValue("p"));
				System.out.println("Finished.!");

			}
			else if (commandLine.hasOption("gcuckoo")) {
				System.out.println("Generating Cuckoo reports...");
				CuckooHandler.submitAllFiles(commandLine.getOptionValue("gcuckoo"), commandLine.getOptionValue("ext"),
						commandLine.hasOption("pwd") ? commandLine.getOptionValue("pwd") : null,
						commandLine.hasOption("time") ? Integer.parseInt(commandLine.getOptionValue("time")) : -1);
				System.out.println("Finished.!");

			} else if (commandLine.hasOption("cpy")) {
				System.out.println("Copying Cuckoo reports...");
				FileHandler.copyFilesFromList(commandLine.getOptionValue("cpy"));
				System.out.println("Finished.!");

			} else if (commandLine.hasOption("gid")) {
				System.out.println("Assigning ID numbers to call functions...");
				FeatureExtractor.generateCallIDsForWildcardModel(
						FileHandler.readConfigValue(Constants.TRAINING_PATH_CONFIG),
						FileHandler.readConfigValue(Constants.TEST_PATH_CONFIG),
						FileHandler.readConfigValue(Constants.FILE_EXTENSION_CONFIG));
				System.out.println("Finished.!");

			}

			else if (commandLine.hasOption("xf")) {
				System.out.println("Extracting and scoring features...");
				FeatureExtractor.generateDistinctiveFeaturesFiles(
						commandLine.hasOption("tp") ? commandLine.getOptionValue("tp")
								: FileHandler.readConfigValue(Constants.TRAINING_PATH_CONFIG),
						commandLine.hasOption("ext") ? commandLine.getOptionValue("ext")
								: FileHandler.readConfigValue(Constants.FILE_EXTENSION_CONFIG),
						false);
				System.out.println("Finished.!");
			}

			else if (commandLine.hasOption("gwf")) {
				System.out.println("Generating Weka Files...");
				String[] inputNumbers = FileHandler.readConfigValue(Constants.NUMBER_OF_DATA_INPUT_CONFIG).split(",");
				ArrayList<Integer> inputSizes = new ArrayList<Integer>();
				for (String s : inputNumbers)
					inputSizes.add(Integer.parseInt(s));
				if (!commandLine.hasOption("tp") && !commandLine.hasOption("vp")) {
					FeatureExtractor.prepareClassifiers(
							commandLine.hasOption("df") ? commandLine.getOptionValue("df")
									: FileHandler.readConfigValue(Constants.DISTINCTIVE_FEATURES_FILE_CONFIG),
							commandLine.hasOption("tp") ? commandLine.getOptionValue("tp")
									: FileHandler.readConfigValue(Constants.TRAINING_PATH_CONFIG),
							commandLine.hasOption("ext") ? commandLine.getOptionValue("ext")
									: FileHandler.readConfigValue(Constants.FILE_EXTENSION_CONFIG),
							false, inputSizes);
					FeatureExtractor.prepareClassifiers(
							commandLine.hasOption("df") ? commandLine.getOptionValue("df")
									: FileHandler.readConfigValue(Constants.DISTINCTIVE_FEATURES_FILE_CONFIG),
							commandLine.hasOption("vp") ? commandLine.getOptionValue("vp")
									: FileHandler.readConfigValue(Constants.TEST_PATH_CONFIG),
							commandLine.hasOption("ext") ? commandLine.getOptionValue("ext")
									: FileHandler.readConfigValue(Constants.FILE_EXTENSION_CONFIG),
							true, inputSizes);
				} else {
					if (commandLine.hasOption("tp")) {
						FeatureExtractor
								.prepareClassifiers(
										commandLine.hasOption("df") ? commandLine.getOptionValue("df")
												: FileHandler
														.readConfigValue(Constants.DISTINCTIVE_FEATURES_FILE_CONFIG),
										commandLine.hasOption("tp") ? commandLine.getOptionValue("tp")
												: FileHandler.readConfigValue(Constants.TRAINING_PATH_CONFIG),
										commandLine.hasOption("ext") ? commandLine.getOptionValue("ext")
												: FileHandler.readConfigValue(Constants.FILE_EXTENSION_CONFIG),
										false, inputSizes);

					}
					if (commandLine.hasOption("vp")) {
						FeatureExtractor
								.prepareClassifiers(
										commandLine.hasOption("df") ? commandLine.getOptionValue("df")
												: FileHandler
														.readConfigValue(Constants.DISTINCTIVE_FEATURES_FILE_CONFIG),
										commandLine.hasOption("vp") ? commandLine.getOptionValue("vp")
												: FileHandler.readConfigValue(Constants.TEST_PATH_CONFIG),
										commandLine.hasOption("ext") ? commandLine.getOptionValue("ext")
												: FileHandler.readConfigValue(Constants.FILE_EXTENSION_CONFIG),
										true, inputSizes);

					}

				}
				System.out.println("Finished.!");
			} else if (commandLine.hasOption("cw")) {
				System.out.println("Classifying with Weka...");

				WekaClassifier.getClassifierResults(commandLine.getOptionValue("wt"), commandLine.getOptionValue("wv"),
						commandLine.hasOption("ext") ? commandLine.getOptionValue("ext")
								: FileHandler.readConfigValue(Constants.FILE_EXTENSION_CONFIG),
						commandLine.getOptionValue("cw"), FileHandler.readConfigValue(Constants.FEATURE_TYPE));
				System.out.println("Finished.!");
			} else if (commandLine.hasOption("cwa")) {
				WekaClassifier.getClassifierResultsForAll(commandLine.getOptionValue("cwa"));
			} else
				System.out.println("Invalid command or arguments!");

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Invalid arguments!");
		}
	}

}
