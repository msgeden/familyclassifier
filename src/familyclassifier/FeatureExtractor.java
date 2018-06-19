/*
x * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package familyclassifier;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import edu.princeton.cs.algs4.Vector;

/**
*
* @author msgeden
*/
public class FeatureExtractor {

	private static HashMap<String, Short[]> constructFeaturesPerSample(HashMap<String, Short[]> cumulativeFeatures,
			String appPath, String extension, boolean isTestData, File reportFile, List<String> featureTypesList) {
		try {

			HashSet<String> features = new HashSet<String>();
			File appFile = new File(appPath);
			String appName = appFile.getName().replace("." + extension, "");

			// Retrieve class type, its name and its integer array index from
			// file path
			String className = "";
			int classFrequencyIndex = -1;
			for (int i = 0; i < Constants.CLASS_NAMES.length; i++) {

				if (appPath.contains(Constants.CLASS_NAMES[i])) {
					className = Constants.CLASS_NAMES[i];
					classFrequencyIndex = i;
				}
			}

			// Performance monitor watch
			StopWatch watch = new StopWatch();
			watch.reset();
			watch.start();
			features = parseFeaturesFromSampleReport(appPath, extension, featureTypesList);
			// Merge feature set with cumulative feature hashmap
			for (String feature : features) {

				if (cumulativeFeatures.containsKey(feature)) {
					Short[] classFrequencies = cumulativeFeatures.get(feature);
					classFrequencies[classFrequencyIndex] = (short) (classFrequencies[classFrequencyIndex] + 1);
					cumulativeFeatures.put(feature, classFrequencies);
				} else {
					Short[] classFrequencies = new Short[Constants.CLASS_NAMES.length];
					for (int i = 0; i < classFrequencies.length; i++)
						classFrequencies[i] = new Short((short) 0);
					classFrequencies[classFrequencyIndex] = 1;
					cumulativeFeatures.put(feature, classFrequencies);
				}
			}

			// These values are stored in features hashmap not to iterate once
			// more during the feature selection calculation
			if (cumulativeFeatures.containsKey(Constants.COUNT_OF_SAMPLES_PER_CLASS)) {
				Short[] appCountsForClasses = cumulativeFeatures.get(Constants.COUNT_OF_SAMPLES_PER_CLASS);
				appCountsForClasses[classFrequencyIndex] = (short) (appCountsForClasses[classFrequencyIndex] + 1);
				cumulativeFeatures.put(Constants.COUNT_OF_SAMPLES_PER_CLASS, appCountsForClasses);
			} else {
				Short[] appCountsForClasses = new Short[Constants.CLASS_NAMES.length];
				for (int i = 0; i < appCountsForClasses.length; i++)
					appCountsForClasses[i] = new Short((short) 0);
				appCountsForClasses[classFrequencyIndex] = 1;
				cumulativeFeatures.put(Constants.COUNT_OF_SAMPLES_PER_CLASS, appCountsForClasses);
			}

			// Print the number of unique features processed.
			FileUtils.write(reportFile,
					className + Constants.TAB_CHAR + extension + Constants.TAB_CHAR + features.size()
							+ Constants.TAB_CHAR + cumulativeFeatures.size() + Constants.TAB_CHAR
							+ (double) watch.getTime() / 1000.0 + Constants.TAB_CHAR + appName + "\n",
					Charset.defaultCharset(), true);
			System.out.print(className + Constants.TAB_CHAR + extension + Constants.TAB_CHAR + features.size()
					+ Constants.TAB_CHAR + cumulativeFeatures.size() + Constants.TAB_CHAR
					+ (double) watch.getTime() / 1000.0 + Constants.TAB_CHAR + appName + "\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return cumulativeFeatures;
	}

	private static HashMap<String, Short[]> constructFeaturesPerSampleForWildcardModel(HashMap<String, String> apicallIDs,
			HashMap<String, Short[]> cumulativeFeatures, HashSet<String> regex, String appPath, String extension,
			boolean isTestData, File reportFile) {
		try {
			String featureType = FileHandler.readConfigValue(Constants.FEATURE_TYPE);
			
			File appFile = new File(appPath);
			String appName = appFile.getName().replace("." + extension, "");

			// Retrieve class type, its name and its integer array index from
			// file path
			String className = "";
			int classFrequencyIndex = -1;
			for (int i = 0; i < Constants.CLASS_NAMES.length; i++) {

				if (appPath.contains(Constants.CLASS_NAMES[i])) {
					className = Constants.CLASS_NAMES[i];
					classFrequencyIndex = i;
				}
			}

			// Performance monitor watch
			StopWatch watch = new StopWatch();
			watch.reset();
			watch.start();
			int numberOfCalls = 0;
			JSONParser parser = new JSONParser();
			FileReader fileReader = new FileReader(appPath);
			JSONObject json = (JSONObject) parser.parse(fileReader);
			String appAPITrace = "";
			JSONObject behavior = (JSONObject) json.get("behavior");
			if (behavior != null) {

				JSONArray processes = (JSONArray) behavior.get("processes");
				if (processes != null) {
					Iterator<JSONObject> iterator1 = processes.iterator();
					JSONArray calls = null;
					while (iterator1.hasNext()) {
						JSONObject process = (JSONObject) iterator1.next();
						String processName = (String) process.get("process_name");
						if (processName.contains(appName)) {
							calls = (JSONArray) process.get("calls");
						}
					}
					if (calls != null) {
						Iterator<JSONObject> iterator2 = calls.iterator();
						ArrayList<JSONObject> callArray = new ArrayList<>();
						numberOfCalls = 0;
						while (iterator2.hasNext()) {
							callArray.add((JSONObject) iterator2.next());
						}
						StringBuilder sb = new StringBuilder();

						for (int i = 0; i < callArray.size(); i++) {
							String key = (String) callArray.get(i).get("api");
							if (featureType.contains(Constants.SYSCALLWILDCARD_FEATURES.substring(1))) {
								if (key.startsWith("Nt"))
								{
									numberOfCalls++;
									String id = apicallIDs.get(key);
									if (i < callArray.size() - 1)
										sb.append(id + "-");
									else
										sb.append(id);
								}
							} else {
								numberOfCalls++;
								String id = apicallIDs.get(key);
								if (i < callArray.size() - 1)
									sb.append(id + "-");
								else
									sb.append(id);
							}
						}
						appAPITrace = sb.toString();
						numberOfCalls = callArray.size();
					}

				}
			}

			// Generate regular expressions for each possible wildcard model
			for (String feature : regex) {
				Pattern p = Pattern.compile(feature);
				Matcher m = p.matcher(appAPITrace);
				boolean found = m.find();
				if (found) {
					if (cumulativeFeatures.containsKey(feature)) {
						Short[] classFrequencies = cumulativeFeatures.get(feature);
						classFrequencies[classFrequencyIndex] = (short) (classFrequencies[classFrequencyIndex] + 1);
						cumulativeFeatures.put(feature, classFrequencies);
					} else {
						Short[] classFrequencies = new Short[Constants.CLASS_NAMES.length];
						for (int i = 0; i < classFrequencies.length; i++)
							classFrequencies[i] = new Short((short) 0);
						classFrequencies[classFrequencyIndex] = 1;
						cumulativeFeatures.put(feature, classFrequencies);
					}
				}
			}

			// These values are stored in features hashmap not to iterate once
			// more during the feature selection calculation
			if (cumulativeFeatures.containsKey(Constants.COUNT_OF_SAMPLES_PER_CLASS)) {
				Short[] appCountsForClasses = cumulativeFeatures.get(Constants.COUNT_OF_SAMPLES_PER_CLASS);
				appCountsForClasses[classFrequencyIndex] = (short) (appCountsForClasses[classFrequencyIndex] + 1);
				cumulativeFeatures.put(Constants.COUNT_OF_SAMPLES_PER_CLASS, appCountsForClasses);
			} else {
				Short[] appCountsForClasses = new Short[Constants.CLASS_NAMES.length];
				for (int i = 0; i < appCountsForClasses.length; i++)
					appCountsForClasses[i] = new Short((short) 0);
				appCountsForClasses[classFrequencyIndex] = 1;
				cumulativeFeatures.put(Constants.COUNT_OF_SAMPLES_PER_CLASS, appCountsForClasses);
			}

			// Print the number of unique features processed.
			FileUtils.write(reportFile,
					className + Constants.TAB_CHAR + extension + Constants.TAB_CHAR + regex.size() + Constants.TAB_CHAR
							+ numberOfCalls + Constants.TAB_CHAR + (double) watch.getTime() / 1000.0
							+ Constants.TAB_CHAR + appName + "\n",
					Charset.defaultCharset(), true);
			System.out.print(className + Constants.TAB_CHAR + extension + Constants.TAB_CHAR + regex.size()
					+ Constants.TAB_CHAR + numberOfCalls + Constants.TAB_CHAR + (double) watch.getTime() / 1000.0
					+ Constants.TAB_CHAR + appName + "\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return cumulativeFeatures;
	}

	private static HashMap<String, Short[]> extractFeatures(String dataPath, String extension, boolean isTestData,
			String[] featureTypes) {

		HashMap<String, Short[]> features = new HashMap<String, Short[]>();
		try {
			ArrayList<File> appPaths = samplePathsOfGivenClass(dataPath, extension, isTestData);
			// Data class label for reports and input files
			String trainOrTestLabel = isTestData ? Constants.TEST_LABEL : Constants.TRAIN_LABEL;
			List<String> featureTypeList = Arrays.asList(featureTypes);

			// Generate report file name
			String reportFilePath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)
					+ (trainOrTestLabel + Constants.UNDERSCORE + extension + Constants.UNDERSCORE
							+ FileHandler.readConfigValue(Constants.FEATURE_TYPE).replaceAll(",", "-").replace("ngram",
									FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "gram")
							+ Constants.UNDERSCORE + "report.tsv").toLowerCase();

			File reportFile = new File(reportFilePath);
			FileUtils.deleteQuietly(reportFile);
			FileUtils.write(reportFile,
					"Number" + Constants.TAB_CHAR + "Class" + Constants.TAB_CHAR + "Extension" + Constants.TAB_CHAR
							+ "# of Features" + Constants.TAB_CHAR + "# of Total Features" + Constants.TAB_CHAR
							+ Constants.TAB_CHAR + "Time Elapsed" + Constants.TAB_CHAR + "Sample Name"
							+ Constants.TAB_CHAR + "\n",
					Charset.defaultCharset(), true);
			System.out.print(
					"Number" + Constants.TAB_CHAR + "Class" + Constants.TAB_CHAR + "Extension" + Constants.TAB_CHAR
							+ "# of Features" + Constants.TAB_CHAR + "# of Total Features" + Constants.TAB_CHAR
							+ "Time Elapsed" + Constants.TAB_CHAR + "Sample Name" + Constants.TAB_CHAR + "\n");
			int count = 0;
			for (File appPath : appPaths) {
				System.out.print((++count) + Constants.TAB_CHAR);
				FileUtils.write(reportFile, count + Constants.TAB_CHAR, Charset.defaultCharset(), true);

				features = constructFeaturesPerSample(features, appPath.getAbsolutePath(), extension, isTestData,
						reportFile, featureTypeList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return features;
	}

	private static HashMap<String, Short[]> extractFeaturesForWildcardModel(String dataPath, String extension,
			boolean isTestData, String[] featureTypes, String regexPattern) {
		String featureType = FileHandler.readConfigValue(Constants.FEATURE_TYPE).replace("ntuple", FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "wildcard");
		HashSet<String> regex = new HashSet<String>();
		HashMap<String, Short[]> features = new HashMap<String, Short[]>();
		try {
			String trainOrTestLabel = isTestData ? Constants.TEST_LABEL : Constants.TRAIN_LABEL;

			// Generate report file name
			String reportFilePath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)
					+ (trainOrTestLabel + Constants.UNDERSCORE + extension + Constants.UNDERSCORE
							+ FileHandler.readConfigValue(Constants.FEATURE_TYPE).replace("ntuple", FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "wildcard").replaceAll(",", "-")
							+ Constants.UNDERSCORE + "report.tsv").toLowerCase();

			File reportFile = new File(reportFilePath);
			FileUtils.deleteQuietly(reportFile);

			ArrayList<File> appPaths = samplePathsOfGivenClass(dataPath, extension, isTestData);
			ArrayList<String> apicallIDVectors = new ArrayList<String>();
			// Read WinAPI IDs from the file
			HashMap<String, String> apicallIDs = new HashMap<String, String>();
			String[] IDFileLines = FileHandler
					.readFileToString(FileHandler.readConfigValue(Constants.ID_MAPPING_FILE) + featureType + ".tsv")
					.split(Constants.NEW_LINE);
			for (String line : IDFileLines) {
				String[] elements = line.split(Constants.TAB_CHAR);
				apicallIDs.put(elements[0], elements[1]);
				apicallIDVectors.add(elements[1]);
			}

			// Generate all possible regex permutations for the given size
			int ngramSize = Integer.parseInt(FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG));
			System.out.print("Generating patterns..." + Constants.NEW_LINE);
			FileUtils.write(reportFile, "Generating patterns..." + Constants.NEW_LINE, Charset.defaultCharset(), true);

			ICombinatoricsVector<String> originalVector = Factory
					.createVector(apicallIDVectors.toArray(new String[apicallIDVectors.size()]));
			Generator gen = Factory.createPermutationWithRepetitionGenerator(originalVector, ngramSize);
			System.out.print("Number of permutations:" + gen.getNumberOfGeneratedObjects() + Constants.NEW_LINE);
			FileUtils.write(reportFile,
					"Number of permutations:" + gen.getNumberOfGeneratedObjects() + Constants.NEW_LINE,
					Charset.defaultCharset(), true);

			List<ICombinatoricsVector<String>> permutations = gen.generateAllObjects();
			for (ICombinatoricsVector<String> permutation : permutations) {
				List<String> calls = permutation.getVector();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < ngramSize; i++) {
					if (i < ngramSize - 1)
						sb.append(calls.get(i) + "-" + regexPattern);
					else
						sb.append(calls.get(i));

				}
				regex.add(sb.toString());
			}
			System.out.print("Number of regex patterns generated:" + regex.size() + Constants.NEW_LINE);

			// Data class label for reports and input files
			FileUtils.write(reportFile,
					"Number" + Constants.TAB_CHAR + "Class" + Constants.TAB_CHAR + "Extension" + Constants.TAB_CHAR
							+ "# of Regex" + Constants.TAB_CHAR + "# of Calls" + Constants.TAB_CHAR + "Time Elapsed"
							+ Constants.TAB_CHAR + "Sample Name" + Constants.TAB_CHAR + "\n",
					Charset.defaultCharset(), true);
			System.out.print("Number" + Constants.TAB_CHAR + "Class" + Constants.TAB_CHAR + "Extension"
					+ Constants.TAB_CHAR + "# of Regex" + Constants.TAB_CHAR + "# of Calls" + Constants.TAB_CHAR
					+ "Time Elapsed" + Constants.TAB_CHAR + "Sample Name" + Constants.TAB_CHAR + "\n");
			int count = 0;
			for (File appPath : appPaths) {
				FileUtils.write(reportFile, (++count) + Constants.TAB_CHAR, Charset.defaultCharset(), true);
				System.out.print(count + Constants.TAB_CHAR);

				features = constructFeaturesPerSampleForWildcardModel(apicallIDs, features, regex, appPath.getAbsolutePath(),
						extension, isTestData, reportFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return features;
	}

	private static String selectDistinctiveFeaturesByClasswiseIG(HashMap<String, Short[]> features, String extension) {
		// Top-ranked feature features file name generation
		String rankedFeaturePath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)
				+ (extension + Constants.UNDERSCORE + "distinctive" + Constants.UNDERSCORE
						+ FileHandler.readConfigValue(Constants.FEATURE_TYPE).replaceAll(",", "-").replace("ngram",
								FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "gram").replace("ntuple",
										FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "wildcard")
						+ Constants.UNDERSCORE + "by_classwiseig.tsv").toLowerCase();
		File rankedFeatureFile = new File(rankedFeaturePath);
		StopWatch watch = new StopWatch();
		watch.reset();
		watch.start();

		// Try to delete if it exists without exception
		FileUtils.deleteQuietly(rankedFeatureFile);
		System.out.println("Analysing Classwise Information Gain Scores...");
		try {

			// This will keep the information gain extracted from each feature
			HashMap<String, Double> featuresWithIG = new HashMap<String, Double>();

			int topRankedSize = Integer.parseInt(FileHandler.readConfigValue(Constants.TOP_RANKED_SIZE_CONFIG));
			int numberOfClass = Constants.CLASS_NAMES.length;
			ScorePair[] topRankedFeatures = new ScorePair[topRankedSize];
			ArrayList<ScorePair[]> topRankedFeaturesForClass = new ArrayList<ScorePair[]>();

			for (int i = 0; i < numberOfClass; i++)
				topRankedFeaturesForClass.add(new ScorePair[topRankedSize / numberOfClass]);

			// This structure will keep the top-ranked features with their
			// information gain
			// This structure is defined to validate the distribution of
			// information gain scores
			ScorePair[] randomFeatures = new ScorePair[topRankedSize];
			// Retrieve the number of apps per class from the specific entry
			Short[] countApps = features.get(Constants.COUNT_OF_SAMPLES_PER_CLASS);
			double[] totalNumberOfAppsInClass = new double[numberOfClass];
			double totalNumberOfApps = 0.0;
			for (int i = 0; i < numberOfClass; i++) {
				totalNumberOfAppsInClass[i] = (double) countApps[i];
				totalNumberOfApps += totalNumberOfAppsInClass[i];
			}
			// Remove this specific entry not to interpret as hash
			// features.remove(Constants.TOTAL_COUNT_OF_APPS_FOR_CLASSES);

			// Iterate feature hashmap to calculate information gain for each
			// feature
			for (Map.Entry<String, Short[]> entry : features.entrySet()) {

				// Calculate the number of apps for each classes that owns the
				// given feature
				Short[] values = entry.getValue();

				// Terms that are needed for information gain calculation
				double[] numberOfAppsHoldFeatureInClass = new double[totalNumberOfAppsInClass.length];
				double[] numberOfAppsDoNotHoldFeatureInClass = new double[totalNumberOfAppsInClass.length];
				for (int i = 0; i < numberOfClass; i++) {
					numberOfAppsHoldFeatureInClass[i] = (double) values[i];
					numberOfAppsDoNotHoldFeatureInClass[i] = (double) (totalNumberOfAppsInClass[i] - values[i]);
				}
				double totalNumberOfAppsHoldFeature = 0.0;
				double totalNumberOfAppsDoNotHoldFeature = 0.0;
				for (int i = 0; i < numberOfClass; i++) {
					totalNumberOfAppsHoldFeature += numberOfAppsHoldFeatureInClass[i];
					totalNumberOfAppsDoNotHoldFeature += numberOfAppsDoNotHoldFeatureInClass[i];
				}
				// Calculation formula of information gain: For details see the
				// paper of Kolter et. al and Reddy et. al.
				double[] informationGainForFeature = new double[totalNumberOfAppsInClass.length * 2];
				for (int i = 0; i < numberOfClass; i++) {
					informationGainForFeature[(i * 2)] = (numberOfAppsHoldFeatureInClass[i]
							/ totalNumberOfAppsInClass[i])
							* (Math.log((numberOfAppsHoldFeatureInClass[i] / totalNumberOfAppsInClass[i])
									/ (((totalNumberOfAppsHoldFeature) / totalNumberOfApps)
											* (1.0 / (double) numberOfClass)))
									/ Math.log(2.0));

					informationGainForFeature[(i * 2)
							+ 1] = (numberOfAppsDoNotHoldFeatureInClass[i] / totalNumberOfAppsInClass[i])
									* (Math.log((numberOfAppsDoNotHoldFeatureInClass[i] / totalNumberOfAppsInClass[i])
											/ (((totalNumberOfAppsDoNotHoldFeature) / totalNumberOfApps)
													* (1.0 / (double) numberOfClass)))
											/ Math.log(2.0));
				}

				// Skip infinite and NaN terms
				double informationGainForFeatureTotal = 0.0;
				for (int i = 0; i < informationGainForFeature.length; i++) {
					if (!(Double.isNaN(informationGainForFeature[i])
							|| !Double.isFinite(informationGainForFeature[i]))) {
						informationGainForFeatureTotal += informationGainForFeature[i];
					}
				}
				// Add calculated information gain for the given feature
				featuresWithIG.put(entry.getKey(), informationGainForFeatureTotal);
			}

			// Initialize top-ranked array values for ordering and comparison
			for (int i = 0; i < numberOfClass; i++) {
				for (int j = 0; j < topRankedSize / numberOfClass; j++) {
					topRankedFeaturesForClass.get(i)[j] = new ScorePair(Integer.toString(i), 0.0);
				}
			}

			// Initialize top-ranked array values for ordering and comparison
			for (int i = 0; i < topRankedFeatures.length; i++) {
				topRankedFeatures[i] = new ScorePair(Integer.toString(i), 0.0);
				randomFeatures[i] = new ScorePair(Integer.toString(i), 0.0);
			}

			for (Map.Entry<String, Double> entry : featuresWithIG.entrySet()) {

				// Find the first minimum value and its index in top-ranked
				// array to replace-> index0:index of the item, index 1-> value
				// of item
				ArrayList<double[]> minIG = new ArrayList<double[]>();

				for (int i = 0; i < numberOfClass; i++)
					minIG.add(minIndexAndValue(topRankedFeaturesForClass.get(i)));

				Short[] counts = features.get(entry.getKey());

				int maxIndex = indexOfMaxValue(counts);

				if (entry.getValue() > minIG.get(maxIndex)[1]) {
					topRankedFeaturesForClass.get(maxIndex)[(int) minIG.get(maxIndex)[0]] = new ScorePair(
							entry.getKey(), entry.getValue());
				}

			}

			// Sort top ranked array and print it to file and console
			for (int i = 0; i < numberOfClass; i++)
				Arrays.sort(topRankedFeaturesForClass.get(i));

			for (int j = 0, g = 0; j < topRankedFeatures.length / numberOfClass; j++)
				for (int i = 0; i < numberOfClass; i++, g++)
					topRankedFeatures[g] = topRankedFeaturesForClass.get(i)[j];

			FileUtils.write(rankedFeatureFile, "Rank" + Constants.TAB_CHAR + "String" + Constants.TAB_CHAR
					+ "Information Gain" + Constants.TAB_CHAR + "Feature", Charset.defaultCharset(), true);

			for (int i = 0; i < Constants.CLASS_NAMES.length; i++)
				FileUtils.write(rankedFeatureFile, Constants.TAB_CHAR + Constants.CLASS_NAMES[i],
						Charset.defaultCharset(), true);

			FileUtils.write(rankedFeatureFile, "\n", Charset.defaultCharset(), true);

			System.out.print("Rank" + Constants.TAB_CHAR + "String" + Constants.TAB_CHAR + "Information Gain"
					+ Constants.TAB_CHAR + "Feature");

			for (int i = 0; i < Constants.CLASS_NAMES.length; i++)
				System.out.print(Constants.TAB_CHAR + Constants.CLASS_NAMES[i]);

			System.out.print("\n");

			for (int i = 0; i < topRankedFeatures.length; i++) {
				if (topRankedFeatures[i].getValue() > 0) {
					String md5 = new String(DigestUtils.md5Hex(topRankedFeatures[i].getKey()));
					Short[] frequencies = features.get(topRankedFeatures[i].getKey());
					FileUtils.write(rankedFeatureFile,
							(i + 1) + Constants.TAB_CHAR + topRankedFeatures[i].getKey() + Constants.TAB_CHAR
									+ topRankedFeatures[i].getValue() + Constants.TAB_CHAR + md5,
							Charset.defaultCharset(), true);
					System.out.print((i + 1) + Constants.TAB_CHAR + topRankedFeatures[i].getKey() + Constants.TAB_CHAR
							+ topRankedFeatures[i].getValue() + Constants.TAB_CHAR + md5);

					for (int j = 0; j < Constants.CLASS_NAMES.length; j++) {
						System.out.print(Constants.TAB_CHAR + frequencies[j]);
						FileUtils.write(rankedFeatureFile, Constants.TAB_CHAR + frequencies[j],
								Charset.defaultCharset(), true);
					}
					FileUtils.write(rankedFeatureFile, "\n", Charset.defaultCharset(), true);
					System.out.print("\n");
				}
			}
			watch.stop();
			// Print the time elapsed to analyze the app.
			System.out.println(
					"Analysis is finished... Elapsed Time:" + (double) watch.getTime() / 1000.0 + " secs." + "\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rankedFeatureFile.getAbsolutePath();
	}

	private static String selectDistinctiveFeaturesByClasswiseNAD(HashMap<String, Short[]> features,
			String extension) {
		// Top-ranked feature features file name generation
		String rankedFeaturePath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)
				+ (extension + Constants.UNDERSCORE + "distinctive" + Constants.UNDERSCORE
						+ FileHandler.readConfigValue(Constants.FEATURE_TYPE).replaceAll(",", "-").replace("ngram",
								FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "gram").replace("ntuple",
										FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "wildcard")
						+ Constants.UNDERSCORE + "by_classwisenad.tsv").toLowerCase();
		File rankedFeatureFile = new File(rankedFeaturePath);
		StopWatch watch = new StopWatch();
		watch.reset();
		watch.start();

		double kValue = Double.parseDouble(FileHandler.readConfigValue(Constants.K_NAD_CONFIG));

		// Try to delete if it exists without exception
		FileUtils.deleteQuietly(rankedFeatureFile);
		System.out.println("Analysing Classwise Normalized Angular Distance Scores...");
		try {

			// This will keep the information gain extracted from each feature
			HashMap<String, Double> featuresWithNAD = new HashMap<String, Double>();

			int topRankedSize = Integer.parseInt(FileHandler.readConfigValue(Constants.TOP_RANKED_SIZE_CONFIG));
			int numberOfClass = Constants.CLASS_NAMES.length;
			ScorePair[] topRankedFeatures = new ScorePair[topRankedSize];
			ArrayList<ScorePair[]> topRankedFeaturesForClass = new ArrayList<ScorePair[]>();

			for (int i = 0; i < numberOfClass; i++)
				topRankedFeaturesForClass.add(new ScorePair[topRankedSize / numberOfClass]);

			// Retrieve the number of apps per class from the specific entry
			Short[] countApps = features.get(Constants.COUNT_OF_SAMPLES_PER_CLASS);

			double[] totalNumberOfAppsInClass = new double[numberOfClass];
			for (int i = 0; i < numberOfClass; i++) {
				totalNumberOfAppsInClass[i] = (double) countApps[i];
			}

			// Remove this specific entry not to interpret as hash
			// features.remove(Constants.TOTAL_COUNT_OF_APPS_FOR_CLASSES);

			// Iterate feature hashmap to calculate information gain for each
			// feature
			double[] prorabilitiesOfReferenceVector = new double[numberOfClass];
			for (int i = 0; i < numberOfClass; i++)
				prorabilitiesOfReferenceVector[i] = 1.0;
			Vector referenceVector = new Vector(prorabilitiesOfReferenceVector);
			double referenceMagnitude = referenceVector.magnitude();
			for (Map.Entry<String, Short[]> entry : features.entrySet()) {

				// Calculate the number of samples for each classes that owns the
				// given feature
				Short[] values = entry.getValue();

				double[] classLikelihoodsOfAFeature = new double[numberOfClass];
				for (int i = 0; i < numberOfClass; i++) {
					classLikelihoodsOfAFeature[i] = (double) values[i] / totalNumberOfAppsInClass[i];
				}
				Vector featureVector = new Vector(classLikelihoodsOfAFeature);

				// Calculate the angle between feature vector and reference
				// vector
				double dotProduct = featureVector.dot(referenceVector);
				double featureMagnitute = featureVector.magnitude();
				double cosinusOfAngle = dotProduct / (referenceMagnitude + featureMagnitute);
				double angle = Math.acos(cosinusOfAngle) / (Math.PI / 4);

				double normalizer = Math.pow(featureMagnitute, 1.0 / kValue);

				double normalizedDistanceScore = normalizer * angle;

				// Add calculated information gain for the given feature
				featuresWithNAD.put(entry.getKey(), normalizedDistanceScore);
			}

			// Initialize top-ranked array values for ordering and comparison
			for (int i = 0; i < numberOfClass; i++) {
				for (int j = 0; j < topRankedSize / numberOfClass; j++) {
					topRankedFeaturesForClass.get(i)[j] = new ScorePair(Integer.toString(i), 0.0);
				}
			}

			// Initialize top-ranked array values for ordering and comparison
			for (int i = 0; i < topRankedFeatures.length; i++) {
				topRankedFeatures[i] = new ScorePair(Integer.toString(i), 0.0);
			}

			for (Map.Entry<String, Double> entry : featuresWithNAD.entrySet()) {

				// Find the first minimum value and its index in top-ranked
				// array to replace-> index0:index of the item, index 1-> value
				// of item
				ArrayList<double[]> minNAD = new ArrayList<double[]>();

				for (int i = 0; i < numberOfClass; i++)
					minNAD.add(minIndexAndValue(topRankedFeaturesForClass.get(i)));

				Short[] counts = features.get(entry.getKey());

				int maxIndex = indexOfMaxValue(counts);

				if (entry.getValue() > minNAD.get(maxIndex)[1]) {
					topRankedFeaturesForClass.get(maxIndex)[(int) minNAD.get(maxIndex)[0]] = new ScorePair(
							entry.getKey(), entry.getValue());
				}

			}

			// Sort top ranked array and print it to file and console
			for (int i = 0; i < numberOfClass; i++)
				Arrays.sort(topRankedFeaturesForClass.get(i));

			for (int j = 0, g = 0; j < topRankedFeatures.length / numberOfClass; j++)
				for (int i = 0; i < numberOfClass; i++, g++)
					topRankedFeatures[g] = topRankedFeaturesForClass.get(i)[j];

			FileUtils.write(
					rankedFeatureFile, "Rank" + Constants.TAB_CHAR + "String" + Constants.TAB_CHAR
							+ "Normalized Angular Distance" + Constants.TAB_CHAR + "Feature",
					Charset.defaultCharset(), true);

			for (int i = 0; i < Constants.CLASS_NAMES.length; i++)
				FileUtils.write(rankedFeatureFile, Constants.TAB_CHAR + Constants.CLASS_NAMES[i],
						Charset.defaultCharset(), true);

			FileUtils.write(rankedFeatureFile, "\n", Charset.defaultCharset(), true);

			System.out.print("Rank" + Constants.TAB_CHAR + "String" + Constants.TAB_CHAR + "Normalized Angular Distance"
					+ Constants.TAB_CHAR + "Feature");

			for (int i = 0; i < Constants.CLASS_NAMES.length; i++)
				System.out.print(Constants.TAB_CHAR + Constants.CLASS_NAMES[i]);

			System.out.print("\n");

			for (int i = 0; i < topRankedFeatures.length; i++) {
				if (topRankedFeatures[i].getValue() > 0) {
					String md5 = new String(DigestUtils.md5Hex(topRankedFeatures[i].getKey()));
					Short[] frequencies = features.get(topRankedFeatures[i].getKey());
					FileUtils.write(rankedFeatureFile,
							(i + 1) + Constants.TAB_CHAR + topRankedFeatures[i].getKey() + Constants.TAB_CHAR
									+ topRankedFeatures[i].getValue() + Constants.TAB_CHAR + md5,
							Charset.defaultCharset(), true);
					System.out.print((i + 1) + Constants.TAB_CHAR + topRankedFeatures[i].getKey() + Constants.TAB_CHAR
							+ topRankedFeatures[i].getValue() + Constants.TAB_CHAR + md5);

					for (int j = 0; j < Constants.CLASS_NAMES.length; j++) {
						System.out.print(Constants.TAB_CHAR + frequencies[j]);
						FileUtils.write(rankedFeatureFile, Constants.TAB_CHAR + frequencies[j],
								Charset.defaultCharset(), true);
					}
					FileUtils.write(rankedFeatureFile, "\n", Charset.defaultCharset(), true);
					System.out.print("\n");
				}
			}
			watch.stop();
			// Print the time elapsed to analyze the sample.
			System.out.println(
					"Analysis is finished... Elapsed Time:" + (double) watch.getTime() / 1000.0 + " secs." + "\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rankedFeatureFile.getAbsolutePath();
	}

	private static String selectDistinctiveFeaturesByIG(HashMap<String, Short[]> features, String extension) {
		// Top-ranked feature features file name generation
		String rankedFeaturePath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)
				+ (extension + Constants.UNDERSCORE + "distinctive" + Constants.UNDERSCORE
						+ FileHandler.readConfigValue(Constants.FEATURE_TYPE).replaceAll(",", "-").replace("ngram",
								FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "gram").replace("ntuple",
										FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "wildcard")
						+ Constants.UNDERSCORE + "by_ig.tsv").toLowerCase();
		File rankedFeatureFile = new File(rankedFeaturePath);
		StopWatch watch = new StopWatch();
		watch.reset();
		watch.start();

		// Try to delete if it exists without exception
		FileUtils.deleteQuietly(rankedFeatureFile);
		System.out.println("Analysing Information Gain Scores...");
		try {

			// This will keep the information gain extracted from each feature
			HashMap<String, Double> featuresWithIG = new HashMap<String, Double>();

			int topRankedSize = Integer.parseInt(FileHandler.readConfigValue(Constants.TOP_RANKED_SIZE_CONFIG));

			ScorePair[] topRankedFeatures = new ScorePair[topRankedSize];

			// Retrieve the number of apps per class from the specific entry
			Short[] countApps = features.get(Constants.COUNT_OF_SAMPLES_PER_CLASS);

			int numberOfClass = Constants.CLASS_NAMES.length;

			double[] totalNumberOfAppsInClass = new double[numberOfClass];
			double totalNumberOfApps = 0.0;
			for (int i = 0; i < numberOfClass; i++) {
				totalNumberOfAppsInClass[i] = (double) countApps[i];
				totalNumberOfApps += totalNumberOfAppsInClass[i];
			}

			// Remove this specific entry not to interpret as hash
			// features.remove(Constants.TOTAL_COUNT_OF_APPS_FOR_CLASSES);

			// Iterate feature hashmap to calculate information gain for each
			// feature
			for (Map.Entry<String, Short[]> entry : features.entrySet()) {

				// Calculate the number of samples for each classes that owns the
				// given feature
				Short[] values = entry.getValue();
				// Terms that are needed for information gain calculation
				double[] numberOfAppsHoldFeatureInClass = new double[totalNumberOfAppsInClass.length];
				double[] numberOfAppsDoNotHoldFeatureInClass = new double[totalNumberOfAppsInClass.length];
				for (int i = 0; i < numberOfClass; i++) {
					numberOfAppsHoldFeatureInClass[i] = (double) values[i];
					numberOfAppsDoNotHoldFeatureInClass[i] = (double) (totalNumberOfAppsInClass[i] - values[i]);
				}

				double totalNumberOfAppsHoldFeature = 0.0;
				double totalNumberOfAppsDoNotHoldFeature = 0.0;
				for (int i = 0; i < numberOfClass; i++) {
					totalNumberOfAppsHoldFeature += numberOfAppsHoldFeatureInClass[i];
					totalNumberOfAppsDoNotHoldFeature += numberOfAppsDoNotHoldFeatureInClass[i];
				}

				// Calculation formula of information gain: For details see the
				// paper of Kolter et. al and Reddy et. al.
				double[] informationGainForFeature = new double[totalNumberOfAppsInClass.length * 2];

				for (int i = 0; i < numberOfClass; i++) {
					informationGainForFeature[(i * 2)] = (numberOfAppsHoldFeatureInClass[i]
							/ totalNumberOfAppsInClass[i])
							* (Math.log((numberOfAppsHoldFeatureInClass[i] / totalNumberOfAppsInClass[i])
									/ (((totalNumberOfAppsHoldFeature) / totalNumberOfApps)
											* (1.0 / (double) numberOfClass)))
									/ Math.log(2.0));

					informationGainForFeature[(i * 2)
							+ 1] = (numberOfAppsDoNotHoldFeatureInClass[i] / totalNumberOfAppsInClass[i])
									* (Math.log((numberOfAppsDoNotHoldFeatureInClass[i] / totalNumberOfAppsInClass[i])
											/ (((totalNumberOfAppsDoNotHoldFeature) / totalNumberOfApps)
													* (1.0 / (double) numberOfClass)))
											/ Math.log(2.0));
				}

				// Skip infinite and NaN terms
				double informationGainForFeatureTotal = 0.0;
				for (int i = 0; i < informationGainForFeature.length; i++) {
					if (!(Double.isNaN(informationGainForFeature[i])
							|| !Double.isFinite(informationGainForFeature[i]))) {
						informationGainForFeatureTotal += informationGainForFeature[i];
					}
				}
				// Add calculated information gain for the given feature
				featuresWithIG.put(entry.getKey(), informationGainForFeatureTotal);
			}

			// Initialize top-ranked array values for ordering and comparison
			for (int i = 0; i < topRankedFeatures.length; i++)
				topRankedFeatures[i] = new ScorePair(Integer.toString(i), 0.0);

			for (Map.Entry<String, Double> entry : featuresWithIG.entrySet()) {

				// Find the first minimum value and its index in top-ranked
				// array to replace-> index0:index of the item, index 1-> value
				// of item
				double[] minIG = minIndexAndValue(topRankedFeatures);
				if (entry.getValue() > minIG[1]) {
					topRankedFeatures[(int) minIG[0]] = new ScorePair(entry.getKey(), entry.getValue());
				}

			}

			// Sort top ranked array and print it to file and console
			Arrays.sort(topRankedFeatures);

			FileUtils.write(rankedFeatureFile, "Rank" + Constants.TAB_CHAR + "String" + Constants.TAB_CHAR
					+ "Information Gain" + Constants.TAB_CHAR + "Feature", Charset.defaultCharset(), true);

			for (int i = 0; i < Constants.CLASS_NAMES.length; i++)
				FileUtils.write(rankedFeatureFile, Constants.TAB_CHAR + Constants.CLASS_NAMES[i],
						Charset.defaultCharset(), true);

			FileUtils.write(rankedFeatureFile, "\n", Charset.defaultCharset(), true);

			System.out.print("Rank" + Constants.TAB_CHAR + "String" + Constants.TAB_CHAR + "Information Gain"
					+ Constants.TAB_CHAR + "Feature");

			for (int i = 0; i < Constants.CLASS_NAMES.length; i++)
				System.out.print(Constants.TAB_CHAR + Constants.CLASS_NAMES[i]);

			System.out.print("\n");

			for (int i = 0; i < topRankedFeatures.length; i++) {
				if (topRankedFeatures[i].getValue() > 0) {
					String md5 = new String(DigestUtils.md5Hex(topRankedFeatures[i].getKey()));
					Short[] frequencies = features.get(topRankedFeatures[i].getKey());
					FileUtils.write(rankedFeatureFile,
							(i + 1) + Constants.TAB_CHAR + topRankedFeatures[i].getKey() + Constants.TAB_CHAR
									+ topRankedFeatures[i].getValue() + Constants.TAB_CHAR + md5,
							Charset.defaultCharset(), true);
					System.out.print((i + 1) + Constants.TAB_CHAR + topRankedFeatures[i].getKey() + Constants.TAB_CHAR
							+ topRankedFeatures[i].getValue() + Constants.TAB_CHAR + md5);

					for (int j = 0; j < Constants.CLASS_NAMES.length; j++) {
						System.out.print(Constants.TAB_CHAR + frequencies[j]);
						FileUtils.write(rankedFeatureFile, Constants.TAB_CHAR + frequencies[j],
								Charset.defaultCharset(), true);
					}
					FileUtils.write(rankedFeatureFile, "\n", Charset.defaultCharset(), true);
					System.out.print("\n");
				}
			}
			watch.stop();
			// Print the time elapsed to analyze the sample.
			System.out.println(
					"Analysis is finished... Elapsed Time:" + (double) watch.getTime() / 1000.0 + " secs." + "\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rankedFeatureFile.getAbsolutePath();
	}

	private static String selectDistinctiveFeaturesByNAD(HashMap<String, Short[]> features, String extension) {
		// Top-ranked feature features file name generation
		String rankedFeaturePath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)
				+ (extension + Constants.UNDERSCORE + "distinctive" + Constants.UNDERSCORE
						+ FileHandler.readConfigValue(Constants.FEATURE_TYPE).replaceAll(",", "-").replace("ngram",
								FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "gram").replace("ntuple",
										FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "wildcard")
						+ Constants.UNDERSCORE + "by_nad.tsv").toLowerCase();
		File rankedFeatureFile = new File(rankedFeaturePath);
		StopWatch watch = new StopWatch();
		watch.reset();
		watch.start();

		double kValue = Double.parseDouble(FileHandler.readConfigValue(Constants.K_NAD_CONFIG));

		// Try to delete if it exists without exception
		FileUtils.deleteQuietly(rankedFeatureFile);
		System.out.println("Analysing Normalized Angular Distance Scores...");
		try {

			// This will keep the normalised angular distance score extracted from each feature
			HashMap<String, Double> featuresWithNAD = new HashMap<String, Double>();

			int topRankedSize = Integer.parseInt(FileHandler.readConfigValue(Constants.TOP_RANKED_SIZE_CONFIG));

			// This structure will keep the top-ranked features with their
			// information gain
			ScorePair[] topRankedFeatures = new ScorePair[topRankedSize];

			// Retrieve the number of apps per class from the specific entry
			Short[] countApps = features.get(Constants.COUNT_OF_SAMPLES_PER_CLASS);

			int numberOfClass = Constants.CLASS_NAMES.length;

			double[] totalNumberOfAppsInClass = new double[numberOfClass];
			for (int i = 0; i < numberOfClass; i++) {
				totalNumberOfAppsInClass[i] = (double) countApps[i];
			}

			// Remove this specific entry not to interpret as hash
			// features.remove(Constants.TOTAL_COUNT_OF_APPS_FOR_CLASSES);

			double[] prorabilitiesOfReferenceVector = new double[numberOfClass];
			for (int i = 0; i < numberOfClass; i++)
				prorabilitiesOfReferenceVector[i] = 1.0;
			Vector referenceVector = new Vector(prorabilitiesOfReferenceVector);
			double referenceMagnitude = referenceVector.magnitude();
			for (Map.Entry<String, Short[]> entry : features.entrySet()) {

				// Calculate the number of apps for each classes that owns the
				// given feature
				Short[] values = entry.getValue();

				double[] classLikelihoodsOfAFeature = new double[numberOfClass];
				for (int i = 0; i < numberOfClass; i++) {
					classLikelihoodsOfAFeature[i] = (double) values[i] / totalNumberOfAppsInClass[i];
				}
				Vector featureVector = new Vector(classLikelihoodsOfAFeature);

				// Calculate the angle between feature vector and reference
				// vector
				double dotProduct = featureVector.dot(referenceVector);
				double featureMagnitute = featureVector.magnitude();
				double cosinusOfAngle = dotProduct / (referenceMagnitude + featureMagnitute);
				double angle = Math.acos(cosinusOfAngle) / (Math.PI / 4);

				double normalizer = Math.pow(featureMagnitute, 1.0 / kValue);

				double normalizedDistanceScore = normalizer * angle;

				// Add calculated information gain for the given feature
				featuresWithNAD.put(entry.getKey(), normalizedDistanceScore);
			}

			// Initialize top-ranked array values for ordering and comparison
			for (int i = 0; i < topRankedFeatures.length; i++)
				topRankedFeatures[i] = new ScorePair(Integer.toString(i), 0.0);

			for (Map.Entry<String, Double> entry : featuresWithNAD.entrySet()) {

				// Find the first minimum value and its index in top-ranked
				// array to replace-> index0:index of the item, index 1-> value
				// of item
				double[] minNAD = minIndexAndValue(topRankedFeatures);
				if (entry.getValue() > minNAD[1]) {
					topRankedFeatures[(int) minNAD[0]] = new ScorePair(entry.getKey(), entry.getValue());
				}
			}

			// Sort top ranked array and print it to file and console
			Arrays.sort(topRankedFeatures);

			FileUtils.write(
					rankedFeatureFile, "Rank" + Constants.TAB_CHAR + "String" + Constants.TAB_CHAR
							+ "Normalized Angular Distance" + Constants.TAB_CHAR + "Feature",
					Charset.defaultCharset(), true);

			for (int i = 0; i < Constants.CLASS_NAMES.length; i++)
				FileUtils.write(rankedFeatureFile, Constants.TAB_CHAR + Constants.CLASS_NAMES[i],
						Charset.defaultCharset(), true);

			FileUtils.write(rankedFeatureFile, "\n", Charset.defaultCharset(), true);

			System.out.print("Rank" + Constants.TAB_CHAR + "String" + Constants.TAB_CHAR + "Normalized Angular Distance"
					+ Constants.TAB_CHAR + "Feature");

			for (int i = 0; i < Constants.CLASS_NAMES.length; i++)
				System.out.print(Constants.TAB_CHAR + Constants.CLASS_NAMES[i]);

			System.out.print("\n");

			for (int i = 0; i < topRankedFeatures.length; i++) {
				if (topRankedFeatures[i].getValue() > 0) {
					String md5 = new String(DigestUtils.md5Hex(topRankedFeatures[i].getKey()));
					Short[] frequencies = features.get(topRankedFeatures[i].getKey());
					FileUtils.write(rankedFeatureFile,
							(i + 1) + Constants.TAB_CHAR + topRankedFeatures[i].getKey() + Constants.TAB_CHAR
									+ topRankedFeatures[i].getValue() + Constants.TAB_CHAR + md5,
							Charset.defaultCharset(), true);
					System.out.print((i + 1) + Constants.TAB_CHAR + topRankedFeatures[i].getKey() + Constants.TAB_CHAR
							+ topRankedFeatures[i].getValue() + Constants.TAB_CHAR + md5);

					for (int j = 0; j < Constants.CLASS_NAMES.length; j++) {
						System.out.print(Constants.TAB_CHAR + frequencies[j]);
						FileUtils.write(rankedFeatureFile, Constants.TAB_CHAR + frequencies[j],
								Charset.defaultCharset(), true);
					}
					FileUtils.write(rankedFeatureFile, "\n", Charset.defaultCharset(), true);
					System.out.print("\n");
				}
			}
			watch.stop();
			// Print the time elapsed to analyze the sample.
			System.out.println(
					"Analysis is finished... Elapsed Time:" + (double) watch.getTime() / 1000.0 + " secs." + "\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rankedFeatureFile.getAbsolutePath();
	}

	public static ArrayList<HashSet<String>> readDistinctiveFeaturesFromFiles(String filePattern,
			ArrayList<Integer> inputSizes) throws IOException {
		ArrayList<HashSet<String>> rankedFeaturesSets = new ArrayList<HashSet<String>>();
		File dir = new File(FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG));
		File[] matchingFiles = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().contains(new File(filePattern).getName())
						&& pathname.getName().endsWith(".tsv");
			}
		});
		File fileIndexes = new File(FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG) + File.separator
				+ "features_distinctive_files_list.txt");
		FileUtils.write(fileIndexes, "pattern:" + filePattern + "\n", Charset.defaultCharset(), true);
		for (int k = 0; k < matchingFiles.length; k++) {
			FileUtils.write(fileIndexes, k + ":" + matchingFiles[k].getName() + "\n", Charset.defaultCharset(), true);
			for (int j = 0; j < inputSizes.size(); j++) {
				HashSet<String> topRankedFeatures = new HashSet<String>();
				List<String> fileLines = FileUtils.readLines(matchingFiles[k], Charset.defaultCharset());
				if (inputSizes.get(j) < fileLines.size()) {
					for (int i = 1; i <= inputSizes.get(j) && i < fileLines.size() - 1; i++) {
						String[] values = fileLines.get(i).split(Constants.TAB_CHAR);
						topRankedFeatures.add(values[1]);
					}
					rankedFeaturesSets.add(topRankedFeatures);
				}
			}
		}
		return rankedFeaturesSets;
	}

	public static void generateDistinctiveFeaturesFiles(String dataPath, String extension, boolean isTestData) {
		String[] featureTypes = FileHandler.readConfigValue(Constants.FEATURE_TYPE)
				.replace("ngram", FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "gram").replace("ntuple", FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "wildcard").split(",");
		HashMap<String, Short[]> features = new HashMap<String, Short[]>();
		if (featureTypes[0].equals(Constants.APICALLWILDCARD_FEATURES.replace("ntuple", FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "wildcard"))||featureTypes[0].equals(Constants.SYSCALLWILDCARD_FEATURES.replace("ntuple", FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "tuple")))
			features = extractFeaturesForWildcardModel(dataPath, extension, isTestData, featureTypes,
					FileHandler.readConfigValue(Constants.REGEX_PATTERN_CONFIG));
		else
			features = extractFeatures(dataPath, extension, isTestData, featureTypes);
		selectDistinctiveFeaturesByIG(features, extension);
		selectDistinctiveFeaturesByClasswiseIG(features, extension);
		selectDistinctiveFeaturesByNAD(features, extension);
		selectDistinctiveFeaturesByClasswiseNAD(features, extension);
		for (int i=0;i<Constants.CLASS_NAMES.length;i++)
			generateHeatMapData(features, extension,featureTypes,i);
	}

	public static void generateHeatMapData(HashMap<String, Short[]> features,String extension, String[] featureTypes, int classIndex) {
		try {
			int ngramSize = Integer.parseInt(FileHandler
					.readConfigValue(Constants.NGRAM_SIZE_CONFIG));
			String featuresStr="";
			for (String s:featureTypes)
				featuresStr+=s+"-";
			int size=Integer.parseInt(FileHandler.readConfigValue(Constants.HEATMAP_SIZE_CONFIG));
			int indexX=0;
			int indexY=0;
			double interval=1.0/size;
			int [][] matrixCounts = new int[size][size];
			for (int i=0; i<size;i++)
				for (int j=0; j<size;j++)
					matrixCounts[i][j]=0;
			String heatMapPath = FileHandler
					.readConfigValue(Constants.REPORTS_PATH_CONFIG) + File.separator + "heatmap" + File.separator
					+ (extension + "-"+Constants.CLASS_NAMES[classIndex]+"-heatmap-"
							+ featuresStr.replace("ngram", ngramSize+"gram") +size+ ".tsv")
							.toLowerCase();
			File heatMap = new File(heatMapPath);
			FileUtils.deleteQuietly(heatMap);
			
			Short[] countApps = features.get(Constants.COUNT_OF_SAMPLES_PER_CLASS);
			features.remove(Constants.COUNT_OF_SAMPLES_PER_CLASS);
			double[] classSizes = new double[]{0,0,0,0};
			double[] otherClassSizes = new double[]{0,0,0,0};
			double totalSize = 0.0;
			for (int i = 0; i < Constants.CLASS_NAMES.length; i++) 
			{
				classSizes[i] = (double) countApps[i];
				totalSize += classSizes[i];
				
			}
			for (int i = 0; i < Constants.CLASS_NAMES.length; i++) 
			{
				otherClassSizes[i]+=totalSize-classSizes[i];
			}
	
			try {
				for (Map.Entry<String, Short[]> entry : features.entrySet()) {

					Short[] values = entry.getValue();
					Double[] probs = new Double[]{0.0,0.0};
					double sumOfOthers = 0.0;
					for (int i = 0; i < Constants.CLASS_NAMES.length; i++) 
						if (i!=classIndex)
							sumOfOthers+=(double)values[i].shortValue();
					
					

					probs[0] =  ((double)values[classIndex].shortValue()/classSizes[classIndex])-0.00000001;
					probs[1]= (sumOfOthers/otherClassSizes[classIndex])-0.00000001;
							
					indexX=(int)((probs[0])/interval);
					indexY=(int)((probs[1]-Double.MIN_VALUE)/interval);
					matrixCounts[indexY][indexX]++;
				}
				double axisY=0.0;
				System.out.print(axisY+Constants.TAB_CHAR);
				FileUtils.write(heatMap,axisY+Constants.TAB_CHAR,Charset.defaultCharset(),true);
				for (int i=0; i<size;i++)
				{
					axisY+=interval;
					System.out.print(String.format("%.3f",axisY)+Constants.TAB_CHAR);
					FileUtils.write(heatMap,String.format("%.3f",axisY)+Constants.TAB_CHAR,Charset.defaultCharset(),true);
				}
				System.out.print("\n");
				FileUtils.write(heatMap,"\n",Charset.defaultCharset(),true);
				for (int i=0; i<size;i++)
				{
					System.out.print(String.format("%.3f",(i+1)*interval)+Constants.TAB_CHAR);
					FileUtils.write(heatMap,String.format("%.3f",(i+1)*interval)+Constants.TAB_CHAR,Charset.defaultCharset(),true);
					for (int j=0; j<size;j++)
					{
						double logOccur = Math.log(matrixCounts[i][j])/Math.log(2);
						if (Double.isInfinite(logOccur))
							logOccur=0.0;
						System.out.print(matrixCounts[i][j]+":"+logOccur+Constants.TAB_CHAR);
						FileUtils.write(heatMap, logOccur+Constants.TAB_CHAR,Charset.defaultCharset(),true);
						
					}
					System.out.print("\n");
					FileUtils.write(heatMap, "\n",Charset.defaultCharset(),true);
					features.put(Constants.COUNT_OF_SAMPLES_PER_CLASS,countApps);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void generateCallIDsForWildcardModel(String trainingDataPath, String testDataPath,
			String extension) {
		String featureType = FileHandler.readConfigValue(Constants.FEATURE_TYPE);
		File mappingIDs = new File(FileHandler.readConfigValue(Constants.ID_MAPPING_FILE) + featureType.replace("ntuple",
				FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "wildcard") + ".tsv");
		FileUtils.deleteQuietly(mappingIDs);
		ArrayList<File> appPaths = samplePathsOfGivenClass(trainingDataPath, extension, false);
		ArrayList<File> testAppPaths = samplePathsOfGivenClass(testDataPath, extension, true);
		appPaths.addAll(testAppPaths);
		HashMap<String, String> apicallIDs = new HashMap<String, String>();

		HashSet<String> apicallNames = new HashSet<String>();
		try {
			// Collect all apicall names
			for (File appPath : appPaths) {
				try {
					String appName = appPath.getName().replace("." + extension, "");
					System.out.println("Reading report file of " + appName + " for apicall ID generation");
					JSONParser parser = new JSONParser();
					FileReader fileReader = new FileReader(appPath);
					JSONObject json = (JSONObject) parser.parse(fileReader);

					JSONObject behavior = (JSONObject) json.get("behavior");
					if (behavior != null) {

						JSONArray processes = (JSONArray) behavior.get("processes");
						if (processes != null) {
							Iterator<JSONObject> iterator1 = processes.iterator();
							JSONArray calls = null;
							while (iterator1.hasNext()) {
								JSONObject process = (JSONObject) iterator1.next();
								String processName = (String) process.get("process_name");
								if (processName.contains(appName)) {
									calls = (JSONArray) process.get("calls");
								}
							}
							if (calls != null) {
								Iterator<JSONObject> iterator2 = calls.iterator();
								ArrayList<JSONObject> callArray = new ArrayList<>();

								while (iterator2.hasNext()) {
									JSONObject call = (JSONObject) iterator2.next();
									String apiName = (String) call.get("api");
									if (featureType.equals(Constants.SYSCALLWILDCARD_FEATURES)) {
										if (apiName.startsWith("Nt"))
											apicallNames.add(apiName);
									} else
										apicallNames.add(apiName);
								}
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			int apicallID = 0;
			for (String apicall : apicallNames)
				if (!apicallIDs.containsKey(apicall)) {
					apicallID++;
					String base36 = Integer.toString(apicallID, 36);
					if (base36.length() == 1)
						base36 = "0" + base36;
					apicallIDs.put(apicall, base36);
				}
			for (String key : apicallIDs.keySet()) {
				System.out.print(key + Constants.TAB_CHAR + apicallIDs.get(key) + Constants.NEW_LINE);
				FileUtils.write(mappingIDs, key + Constants.TAB_CHAR + apicallIDs.get(key) + Constants.NEW_LINE,
						Charset.defaultCharset(), true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static HashSet<String> parseFeaturesFromSampleReport(String appPath, String extension, List<String> featureTypeList) {

		HashSet<String> features = new HashSet<String>();
		try {
			File appFile = new File(appPath);
			String appName = appFile.getName().replace("." + extension, "");
			int ngramSize = Integer.parseInt(FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG));

			if (featureTypeList.contains(Constants.NGRAM_FEATURES.replace("ngram", ngramSize + "gram"))) {
				byte fileHex[];

				fileHex = FileHandler.readFileToByteArray(appFile.getAbsolutePath());
				for (int i = 0; i < fileHex.length - (ngramSize - 1); i++) {
					StringBuilder sb = new StringBuilder();
					for (int j = 0; j < ngramSize; j++) {
						// if (fileHex[i + j] != 0)
						sb.append(String.format("%02X", fileHex[i + j]));

					}
					String ngram = sb.toString();
					features.add(ngram);
				}
			} else {
				JSONParser parser = new JSONParser();
				FileReader fileReader = new FileReader(appPath);
				JSONObject json = (JSONObject) parser.parse(fileReader);
				if (featureTypeList.contains(Constants.APICALL_FEATURES)) {
					JSONObject behavior = (JSONObject) json.get("behavior");
					if (behavior != null) {

						JSONArray processes = (JSONArray) behavior.get("processes");
						if (processes != null) {
							Iterator<JSONObject> iterator1 = processes.iterator();
							JSONArray calls = null;
							while (iterator1.hasNext()) {
								JSONObject process = (JSONObject) iterator1.next();
								String processName = (String) process.get("process_name");
								if (processName.contains(appName)) {
									calls = (JSONArray) process.get("calls");
								}
							}
							if (calls != null) {
								Iterator<JSONObject> iterator2 = calls.iterator();

								while (iterator2.hasNext()) {
									JSONObject call = (JSONObject) iterator2.next();
									String apiName = (String) call.get("api");
									features.add(apiName);
								}
							}
						}
					}
					//if (features.size() == 0)
					//	FileUtils.deleteQuietly(new File (appPath));
				}
				if (featureTypeList.contains(Constants.APICALLNGRAM_FEATURES.replace("ngram", ngramSize + "gram"))) {
					JSONObject behavior = (JSONObject) json.get("behavior");
					if (behavior != null) {

						JSONArray processes = (JSONArray) behavior.get("processes");
						if (processes != null) {
							Iterator<JSONObject> iterator1 = processes.iterator();
							JSONArray calls = null;
							while (iterator1.hasNext()) {
								JSONObject process = (JSONObject) iterator1.next();
								String processName = (String) process.get("process_name");
								if (processName.contains(appName)) {
									calls = (JSONArray) process.get("calls");
								}
							}
							if (calls != null) {
								Iterator<JSONObject> iterator2 = calls.iterator();
								ArrayList<JSONObject> callArray = new ArrayList<>();
								while (iterator2.hasNext())
									callArray.add((JSONObject) iterator2.next());
								for (int i = 0; i < callArray.size() - (ngramSize - 1); i++) {
									StringBuilder sb = new StringBuilder();
									for (int j = 0; j < ngramSize; j++) {

										// sb.append(String.format("%02X",
										// fileHex[i
										// + j]));
										sb.append((String) callArray.get(i + j).get("api"));

									}
									String ngram = sb.toString();
									features.add(ngram);
								}
							}
						}
					}
				}

				if (featureTypeList.contains(Constants.APIWITHARGS_FEATURES)) {
					JSONObject behavior = (JSONObject) json.get("behavior");
					if (behavior != null) {

						JSONArray processes = (JSONArray) behavior.get("processes");
						if (processes != null) {
							Iterator<JSONObject> iterator1 = processes.iterator();
							JSONArray calls = null;
							while (iterator1.hasNext()) {
								JSONObject process = (JSONObject) iterator1.next();
								String processName = (String) process.get("process_name");
								if (processName.contains(appName)) {
									calls = (JSONArray) process.get("calls");
								}
							}
							if (calls != null) {
								Iterator<JSONObject> iterator2 = calls.iterator();

								while (iterator2.hasNext()) {
									JSONObject call = (JSONObject) iterator2.next();
									String apiName = (String) call.get("api");
									JSONObject args = (JSONObject) call.get("arguments");
									Set<String> arguments = args.keySet();
									String argsValues = "";
									int size = arguments.size();
									int counter = 0;
									for (String key : arguments)
									{
										if (size == ++counter)
											argsValues += args.get(key);
										else
											argsValues += args.get(key)+",";
									}					
									argsValues=argsValues.replaceAll("[^A-Za-z0-9,]", "");
									//File functionsLog = new File(FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)+"functions.log");
									//FileUtils.write(functionsLog, "\n"+apiName+"(" + argsValues +")" , Charset.defaultCharset(), true);
									if (argsValues.length()>200)
										argsValues=argsValues.substring(0, 200);
									//System.out.println(apiName+"(" + argsValues +")" );
									
									String argsHash = new String(DigestUtils.md5Hex(argsValues));
									//features.add(apiName + argsHash);
									features.add(apiName + "("+argsValues+")");
								}
							}
						}
					}

				}
				if (featureTypeList.contains(Constants.SYSCALL_FEATURES)) {
					JSONObject behavior = (JSONObject) json.get("behavior");
					if (behavior != null) {

						JSONArray processes = (JSONArray) behavior.get("processes");
						if (processes != null) {
							Iterator<JSONObject> iterator1 = processes.iterator();
							JSONArray calls = null;
							while (iterator1.hasNext()) {
								JSONObject process = (JSONObject) iterator1.next();
								String processName = (String) process.get("process_name");
								if (processName.contains(appName)) {
									calls = (JSONArray) process.get("calls");
								}
							}
							if (calls != null) {
								Iterator<JSONObject> iterator2 = calls.iterator();

								while (iterator2.hasNext()) {
									JSONObject call = (JSONObject) iterator2.next();
									String apiName = (String) call.get("api");
									if (apiName.startsWith("Nt"))
										features.add(apiName);
								}
							}
						}
					}

				}
				if (featureTypeList.contains(Constants.SYSCALLNGRAM_FEATURES.replace("ngram", ngramSize + "gram"))) {
					JSONObject behavior = (JSONObject) json.get("behavior");
					if (behavior != null) {

						JSONArray processes = (JSONArray) behavior.get("processes");
						if (processes != null) {
							Iterator<JSONObject> iterator1 = processes.iterator();
							JSONArray calls = null;
							while (iterator1.hasNext()) {
								JSONObject process = (JSONObject) iterator1.next();
								String processName = (String) process.get("process_name");
								if (processName.contains(appName)) {
									calls = (JSONArray) process.get("calls");
								}
							}
							if (calls != null) {
								Iterator<JSONObject> iterator2 = calls.iterator();
								ArrayList<JSONObject> callArray = new ArrayList<>();
								while (iterator2.hasNext())
								{
									JSONObject call = (JSONObject) iterator2.next();
									String api = (String) call.get("api");
									if (api.startsWith("Nt"))
									{
										callArray.add(call);
									}
								}
								for (int i = 0; i < callArray.size() - (ngramSize - 1); i++) {
									StringBuilder sb = new StringBuilder();
									for (int j = 0; j < ngramSize; j++) {
										// sb.append(String.format("%02X",
										// fileHex[i
										// + j]));
										String api = (String) callArray.get(i + j).get("api");
										sb.append(api);
								
									}
									String ngram = sb.toString();
									features.add(ngram);
								}
							}
						}
					}
				}

				if (featureTypeList.contains(Constants.SYSCALLWITHARGS_FEATURES)) {
					JSONObject behavior = (JSONObject) json.get("behavior");
					if (behavior != null) {

						JSONArray processes = (JSONArray) behavior.get("processes");
						if (processes != null) {
							Iterator<JSONObject> iterator1 = processes.iterator();
							JSONArray calls = null;
							while (iterator1.hasNext()) {
								JSONObject process = (JSONObject) iterator1.next();
								String processName = (String) process.get("process_name");
								if (processName.contains(appName)) {
									calls = (JSONArray) process.get("calls");
								}
							}
							if (calls != null) {
								Iterator<JSONObject> iterator2 = calls.iterator();

								while (iterator2.hasNext()) {
									JSONObject call = (JSONObject) iterator2.next();
									String apiName = (String) call.get("api");
									if (apiName.startsWith("Nt")) {
										JSONObject args = (JSONObject) call.get("arguments");
										Set<String> arguments = args.keySet();
										String argsValues = "";
										//for (String key : arguments)
										//	argsValues += args.get(key);
										//String argsHash = new String(DigestUtils.md5Hex(argsValues));
										//features.add(apiName + argsHash);
										
										int size = arguments.size();
										int counter = 0;
										for (String key : arguments)
										{
											if (size == ++counter)
												argsValues += args.get(key);
											else
												argsValues += args.get(key)+",";
										}					
										argsValues=argsValues.replaceAll("[^A-Za-z0-9,]", "");
										//File functionsLog = new File(FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)+"functions.log");
										//FileUtils.write(functionsLog, "\n"+apiName+"(" + argsValues +")" , Charset.defaultCharset(), true);
										if (argsValues.length()>200)
											argsValues=argsValues.substring(0, 200);
										//System.out.println(apiName+"(" + argsValues +")" );
										
										String argsHash = new String(DigestUtils.md5Hex(argsValues));
										//features.add(apiName + argsHash);
										features.add(apiName + "("+argsValues+")");
									}
								}
							}
						}
					}

				}

				if (featureTypeList.contains(Constants.DLL_FEATURES)) {
					JSONObject behavior = (JSONObject) json.get("behavior");
					if (behavior != null) {
						JSONObject summary = (JSONObject) behavior.get("summary");
						if (summary != null) {
							JSONArray dlls = (JSONArray) summary.get("dll_loaded");
							if (dlls != null) {
								Iterator<String> iterator1 = dlls.iterator();
								while (iterator1.hasNext()) {
									String dllName = (String) iterator1.next();
									// features.add(dllName);
									features.add(dllName.substring(dllName.lastIndexOf("\\") + 1));
								}
							}
						}
					}

				}
				if (featureTypeList.contains(Constants.REGKEY_FEATURES)) {

					JSONObject behavior = (JSONObject) json.get("behavior");
					if (behavior != null) {
						JSONObject summary = (JSONObject) behavior.get("summary");
						if (summary != null) {
							JSONArray regkeyopened = (JSONArray) summary.get("regkey_opened");
							if (regkeyopened != null) {
								Iterator<String> iterator1 = regkeyopened.iterator();
								while (iterator1.hasNext()) {
									String regKeyName = (String) iterator1.next();
									features.add(regKeyName);
								}
							}
							JSONArray regkeysread = (JSONArray) summary.get("regkey_read");
							if (regkeysread != null) {
								Iterator<String> iterator1 = regkeysread.iterator();
								while (iterator1.hasNext()) {
									String regKeyName = (String) iterator1.next();
									features.add(regKeyName);
								}
							}
						}
					}
				}
				if (featureTypeList.contains(Constants.MUTEX_FEATURES)) {

					JSONObject behavior = (JSONObject) json.get("behavior");
					if (behavior != null) {
						JSONObject summary = (JSONObject) behavior.get("summary");
						if (summary != null) {
							JSONArray mutex = (JSONArray) summary.get("mutex");
							if (mutex != null) {
								Iterator<String> iterator1 = mutex.iterator();
								while (iterator1.hasNext()) {
									String mutexName = (String) iterator1.next();
									features.add(mutexName);
								}
							}
						}
					}
				}

				if (featureTypeList.contains(Constants.FILE_FEATURES)) {
					JSONObject behavior = (JSONObject) json.get("behavior");
					if (behavior != null) {
						JSONObject summary = (JSONObject) behavior.get("summary");
						if (summary != null) {
							JSONArray filescreated = (JSONArray) summary.get("file_created");
							if (filescreated != null) {
								Iterator<String> iterator1 = filescreated.iterator();
								while (iterator1.hasNext()) {
									String fileName = (String) iterator1.next();
									// features.add(dllName);
									features.add(fileName.substring(fileName.lastIndexOf("\\") + 1));
								}
							}
							JSONArray filesopened = (JSONArray) summary.get("file_opened");
							if (filesopened != null) {
								Iterator<String> iterator1 = filesopened.iterator();
								while (iterator1.hasNext()) {
									String fileName = (String) iterator1.next();
									// features.add(dllName);
									features.add(fileName.substring(fileName.lastIndexOf("\\") + 1));
								}
							}
							JSONArray fileswritten = (JSONArray) summary.get("file_written");
							if (fileswritten != null) {
								Iterator<String> iterator1 = fileswritten.iterator();
								while (iterator1.hasNext()) {
									String fileName = (String) iterator1.next();
									// features.add(dllName);
									features.add(fileName.substring(fileName.lastIndexOf("\\") + 1));
								}
							}
							JSONArray filesread = (JSONArray) summary.get("file_read");
							if (filesread != null) {
								Iterator<String> iterator1 = filesread.iterator();
								while (iterator1.hasNext()) {
									String fileName = (String) iterator1.next();
									// features.add(dllName);
									features.add(fileName.substring(fileName.lastIndexOf("\\") + 1));
								}
							}
							JSONArray filesfailed = (JSONArray) summary.get("file_failed");
							if (filesfailed != null) {
								Iterator<String> iterator1 = filesfailed.iterator();
								while (iterator1.hasNext()) {
									String fileName = (String) iterator1.next();
									// features.add(dllName);
									features.add(fileName.substring(fileName.lastIndexOf("\\") + 1));
								}
							}

						}
					}

				}
				if (featureTypeList.contains(Constants.APIFREQUENCY_FEATURES)) {
					JSONObject behavior = (JSONObject) json.get("behavior");
					JSONArray generics = (JSONArray) behavior.get("generic");
					Iterator<JSONObject> iterator1 = generics.iterator();
					Long pid = null;
					while (iterator1.hasNext()) {
						JSONObject generic = (JSONObject) iterator1.next();
						if (((String) generic.get("process_name")).contains(appName)) {
							pid = (Long) generic.get("pid");
						}
					}

					JSONObject apistats = (JSONObject) behavior.get("apistats");
					JSONObject apistatsOfProcess = (JSONObject) apistats.get(Long.toString(pid));
					HashMap<String, Long> apistatsHM = new HashMap<>();
					Set<String> apistatKeys = apistatsOfProcess.keySet();

					for (String key : apistatKeys)
						apistatsHM.put(key, (Long) apistatsOfProcess.get(key));

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return features;
	}

	private static String writeFeatureAttributesToWekaFile(HashSet<String> features, String extension, boolean isTestData,
			int numberOfDataInput, String featureType, String extractionMethodology) throws IOException {
		// Data class label for reports and input files

		String trainOrTestLabel = isTestData ? Constants.TEST_LABEL : Constants.TRAIN_LABEL;

		// Generate data file and prepare it with its headers
		String wekaDataFilePath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG) + trainOrTestLabel
				+ File.separator
				+ (trainOrTestLabel + Constants.UNDERSCORE + extension + Constants.UNDERSCORE
						+ featureType.replace("ngram",
								FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "gram")
						+ Constants.UNDERSCORE + numberOfDataInput + Constants.UNDERSCORE + extractionMethodology
						+ ".arff").toLowerCase();

		File wekaDataFile = new File(wekaDataFilePath);
		FileUtils.deleteQuietly(wekaDataFile);

		FileUtils.write(wekaDataFile,
				"%%%\n" + "% This " + trainOrTestLabel.toLowerCase()
						+ " data file consists of the most distictive features extracted\n" + "% from " + extension
						+ " reports generated by Cuckoo Sandbox\n"
						+ "% via dynamic analysis performed on known malware families\n"
						+ "% to classify unknown malware families\n"
						+ "% by using Weka classifier algorithms. The study is being conducted\n"
						+ "% by Munir Geden and Dr. Jassim Happa as part of a research at Cyber Security Centre of University of Oxford.\n"
						+ "%\n" + "@relation 'feature'\n",
				Charset.defaultCharset(), true);

		// Append appname to arff file
		FileUtils.write(wekaDataFile, "@attribute appname string\n", Charset.defaultCharset(), true);
		for (String entry : features) {
			String attribute = "";
			// if (featureType.equals(Constants.APICALL_FEATURES) ||
			// featureType.equals(Constants.DLL_FEATURES)
			// || featureType.equals(Constants.APIWITHARGS_FEATURES))
			// attribute = entry;
			// else
			attribute = new String(DigestUtils.md5Hex(entry));
			FileUtils.write(wekaDataFile, "@attribute " + attribute + " numeric\n", Charset.defaultCharset(), true);
		}
		FileUtils.write(wekaDataFile, "@attribute class {", Charset.defaultCharset(), true);
		for (int i = 0; i < Constants.CLASS_NAMES.length; i++) {
			if (i == 0)
				FileUtils.write(wekaDataFile, Constants.CLASS_NAMES[i], Charset.defaultCharset(), true);
			else
				FileUtils.write(wekaDataFile, "," + Constants.CLASS_NAMES[i], Charset.defaultCharset(), true);
		}
		FileUtils.write(wekaDataFile, "}\n", Charset.defaultCharset(), true);
		FileUtils.write(wekaDataFile, "@data\n", Charset.defaultCharset(), true);
		return wekaDataFilePath;
	}

	private static void writeSampleVectorToWekaFile(ArrayList<HashSet<String>> topRankedFeaturesListSet, String appPath,
			ArrayList<String> dataFilePaths, String extension, boolean isTestData, List<String> featureTypes) {
		try {
			HashSet<String> features = new HashSet<String>();

			// Arff file that will keep training data values
			ArrayList<File> dataFiles = new ArrayList<File>();
			for (String s : dataFilePaths)
				dataFiles.add(new File(s));

			File file = new File(appPath);

			ArrayList<HashMap<String, Integer>> calculatedDatas = new ArrayList<HashMap<String, Integer>>();
			for (HashSet<String> rankedList : topRankedFeaturesListSet) {
				HashMap<String, Integer> calculatedData = new HashMap<String, Integer>();
				for (String entry : rankedList) {
					calculatedData.put(entry, 0);
				}
				calculatedDatas.add(calculatedData);
			}

			// Retrieve class type, its name and its integer array index from
			// file path
			String className = "";
			// int classFrequencyIndex = -1;
			for (int i = 0; i < Constants.CLASS_NAMES.length; i++) {

				if (appPath.contains(Constants.CLASS_NAMES[i])) {
					className = Constants.CLASS_NAMES[i];
					// classFrequencyIndex = i;
				}
			}
			// Find appName from path
			String appName = file.getName().replace("." + extension, "");

			// Print iteration information for console
			System.out.print("Analysing sample... Class:" + className + Constants.TAB_CHAR + "Extension:" + extension
					+ Constants.TAB_CHAR + "Sample Name:" + appName + "\n");

			// Number of files traversed
			int fileIterator = 0;

			// Performance monitor watch
			StopWatch watch = new StopWatch();
			watch.reset();
			watch.start();
			features = parseFeaturesFromSampleReport(appPath, extension, featureTypes);

			for (String feature : features) {
				for (int k = 0; k < topRankedFeaturesListSet.size(); k++) {
					if (topRankedFeaturesListSet.get(k).contains(feature)) {
						if (calculatedDatas.get(k).containsKey(feature)) {
							calculatedDatas.get(k).put(feature, 1);
						}
					}
				}
			}

			watch.stop();
			// Print the time elapsed to analyze the sample.
			System.out.print("Completed... Elapsed Time:" + (double) watch.getTime() / 1000.0 + " secs."
					+ Constants.TAB_CHAR + "Number of Files:" + (fileIterator + 1) + "\n");

			for (int k = 0; k < topRankedFeaturesListSet.size(); k++) {

				// Append sample inputs to arff file
				FileUtils.write(dataFiles.get(k), appName + ",", Charset.defaultCharset(), true);
				for (Map.Entry<String, Integer> entry : calculatedDatas.get(k).entrySet()) {
					FileUtils.write(dataFiles.get(k), entry.getValue() + ",", Charset.defaultCharset(), true);
				}
				FileUtils.write(dataFiles.get(k), className + "\n", Charset.defaultCharset(), true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void writeSampleVectorOfWildcardModelToWekaFile(HashMap<String, String> apicallIDs,
			ArrayList<HashSet<String>> topRankedFeaturesListSet, String appPath, ArrayList<String> dataFilePaths,
			String extension, boolean isTestData, List<String> featureTypes) {
		try {
			// Arff file that will keep training data values
			ArrayList<File> dataFiles = new ArrayList<File>();
			for (String s : dataFilePaths)
				dataFiles.add(new File(s));

			File file = new File(appPath);

			ArrayList<HashMap<String, Integer>> calculatedDatas = new ArrayList<HashMap<String, Integer>>();
			for (HashSet<String> rankedList : topRankedFeaturesListSet) {
				HashMap<String, Integer> calculatedData = new HashMap<String, Integer>();
				for (String entry : rankedList) {
					calculatedData.put(entry, 0);
				}
				calculatedDatas.add(calculatedData);
			}

			// Retrieve class type, its name and its integer array index from
			// file path
			String className = "";
			// int classFrequencyIndex = -1;
			for (int i = 0; i < Constants.CLASS_NAMES.length; i++) {

				if (appPath.contains(Constants.CLASS_NAMES[i])) {
					className = Constants.CLASS_NAMES[i];
					// classFrequencyIndex = i;
				}
			}
			// Find appName from path
			String appName = file.getName().replace("." + extension, "");

			// Print iteration information for console
			System.out.print("Analysing sample... Class:" + className + Constants.TAB_CHAR + "Extension:" + extension
					+ Constants.TAB_CHAR + "Sample Name:" + appName + "\n");

			// Number of files traversed
			int fileIterator = 0;

			// Performance monitor watch
			StopWatch watch = new StopWatch();
			watch.reset();
			watch.start();

			JSONParser parser = new JSONParser();
			FileReader fileReader = new FileReader(appPath);
			JSONObject json = (JSONObject) parser.parse(fileReader);
			String appAPITrace = "";
			JSONObject behavior = (JSONObject) json.get("behavior");
			if (behavior != null) {

				JSONArray processes = (JSONArray) behavior.get("processes");
				if (processes != null) {
					Iterator<JSONObject> iterator1 = processes.iterator();
					JSONArray calls = null;
					while (iterator1.hasNext()) {
						JSONObject process = (JSONObject) iterator1.next();
						String processName = (String) process.get("process_name");
						if (processName.contains(appName)) {
							calls = (JSONArray) process.get("calls");
						}
					}
					if (calls != null) {
						Iterator<JSONObject> iterator2 = calls.iterator();
						ArrayList<JSONObject> callArray = new ArrayList<>();
						while (iterator2.hasNext()) {
							callArray.add((JSONObject) iterator2.next());
						}
						StringBuilder sb = new StringBuilder();
						for (int i = 0; i < callArray.size(); i++) {
							String key = (String) callArray.get(i).get("api");
							if (featureTypes.get(0).equals(Constants.SYSCALLWILDCARD_FEATURES.substring(1))) {
								if (key.startsWith("Nt")) {
									String id = apicallIDs.get(key);
									if (i < callArray.size() - 1)
										sb.append(id + "-");
									else
										sb.append(id);
								}
							} else {
								String id = apicallIDs.get(key);
								if (i < callArray.size() - 1)
									sb.append(id + "-");
								else
									sb.append(id);
							}

						}
						appAPITrace = sb.toString();

					}

				}
			}

			// Generate regular expressions for each possible wildcard

			for (int k = 0; k < topRankedFeaturesListSet.size(); k++) {
				// HashMap<String, Integer> calculatedData = new HashMap<String,
				// Integer>();
				for (String feature : topRankedFeaturesListSet.get(k)) {
					Pattern p = Pattern.compile(feature);
					Matcher m = p.matcher(appAPITrace);

					boolean found = m.find();
					if (found) {
						if (calculatedDatas.get(k).containsKey(feature)) {
							calculatedDatas.get(k).put(feature, 1);
						}
					}
				}
			}

			watch.stop();
			// Print the time elapsed to analyze the sample.
			System.out.print("Completed... Elapsed Time:" + (double) watch.getTime() / 1000.0 + " secs."
					+ Constants.TAB_CHAR + "Number of Files:" + (fileIterator + 1) + "\n");

			for (int k = 0; k < topRankedFeaturesListSet.size(); k++) {

				// Append sample inputs to arff file
				FileUtils.write(dataFiles.get(k), appName + ",", Charset.defaultCharset(), true);
				for (Map.Entry<String, Integer> entry : calculatedDatas.get(k).entrySet()) {
					FileUtils.write(dataFiles.get(k), entry.getValue() + ",", Charset.defaultCharset(), true);
				}
				FileUtils.write(dataFiles.get(k), className + "\n", Charset.defaultCharset(), true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void prepareClassifiers(String topRankedFeaturesFile, String dataPath, String extension,
			boolean isTestData, ArrayList<Integer> inputSizes) {
		String[] featureTypes = FileHandler.readConfigValue(Constants.FEATURE_TYPE).split(",");
		if (featureTypes[0].equals(Constants.APICALLWILDCARD_FEATURES)||featureTypes[0].equals(Constants.SYSCALLWILDCARD_FEATURES))
			prepareWekaFilesForWildcardModel(topRankedFeaturesFile, dataPath, extension, isTestData, inputSizes);
		else
			prepareWekaFiles(topRankedFeaturesFile, dataPath, extension, isTestData, inputSizes);

	}

	public static void prepareWekaFiles(String topRankedFeaturesFile, String dataPath, String extension,
			boolean isTestData, ArrayList<Integer> inputSizes) {
		try {

			String[] featureTypes = FileHandler.readConfigValue(Constants.FEATURE_TYPE)
					.replace("ngram", FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG) + "gram").split(",");
			List<String> featureTypesList = Arrays.asList(featureTypes);
			ArrayList<HashSet<String>> rankedFeaturesSets = readDistinctiveFeaturesFromFiles(topRankedFeaturesFile,
					inputSizes);
			ArrayList<String> dataFilePaths = new ArrayList<String>();
			for (int i = 0; i < rankedFeaturesSets.size(); i++) {
				HashSet<String> topRankedFeatures = rankedFeaturesSets.get(i);
				String dataFilePath = writeFeatureAttributesToWekaFile(topRankedFeatures, extension, isTestData,
						inputSizes.get(i % inputSizes.size()),
						FileHandler.readConfigValue(Constants.FEATURE_TYPE).replaceAll(",", "-"),
						Integer.toString(i / inputSizes.size()));
				dataFilePaths.add(dataFilePath);
			}
			ArrayList<File> appPaths = samplePathsOfGivenClass(dataPath, extension, isTestData);
			for (File appPath : appPaths) {
				writeSampleVectorToWekaFile(rankedFeaturesSets, appPath.getAbsolutePath(), dataFilePaths, extension,
						isTestData, featureTypesList);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void prepareWekaFilesForWildcardModel(String topRankedFeaturesFile, String dataPath, String extension,
			boolean isTestData, ArrayList<Integer> inputSizes) {
		try {
			String ngramSize = FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG);
			String[] featureTypes = FileHandler.readConfigValue(Constants.FEATURE_TYPE).replace("ntuple",
					ngramSize + "wildcard").split(",");
			List<String> featureTypesList = Arrays.asList(featureTypes);
			ArrayList<HashSet<String>> rankedFeaturesSets = readDistinctiveFeaturesFromFiles(topRankedFeaturesFile,
					inputSizes);
			ArrayList<String> dataFilePaths = new ArrayList<String>();
			for (int i = 0; i < rankedFeaturesSets.size(); i++) {
				HashSet<String> topRankedFeatures = rankedFeaturesSets.get(i);
				String dataFilePath = writeFeatureAttributesToWekaFile(topRankedFeatures, extension, isTestData,
						inputSizes.get(i % inputSizes.size()),
						FileHandler.readConfigValue(Constants.FEATURE_TYPE).replace("ntuple",
								ngramSize + "wildcard").replaceAll(",", "-"),
						Integer.toString(i / inputSizes.size()));
				dataFilePaths.add(dataFilePath);
			}
			ArrayList<File> appPaths = samplePathsOfGivenClass(dataPath, extension, isTestData);
			HashMap<String, String> apicallIDs = new HashMap<String, String>();
			String[] IDFileLines = FileHandler
					.readFileToString(FileHandler.readConfigValue(Constants.ID_MAPPING_FILE) + featureTypes[0] + ".tsv")
					.split(Constants.NEW_LINE);

			for (String line : IDFileLines) {
				String[] elements = line.split(Constants.TAB_CHAR);
				apicallIDs.put(elements[0], elements[1]);
			}

			for (File appPath : appPaths) {
				writeSampleVectorOfWildcardModelToWekaFile(apicallIDs, rankedFeaturesSets, appPath.getAbsolutePath(), dataFilePaths,
						extension, isTestData, featureTypesList);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static ArrayList<File> samplePathsOfGivenClass(String dataPath, String extension, boolean isTestData) {
		String dataTypeFolder = "";
		if (!(dataPath.contains(File.separator + Constants.TEST_LABEL + File.separator)
				|| dataPath.contains(File.separator + Constants.TRAIN_LABEL + File.separator))) {
			dataTypeFolder = isTestData ? Constants.TEST_LABEL : Constants.TRAIN_LABEL;
		}
		ArrayList<File> appPaths = new ArrayList<File>();
		for (int i = 0; i < Constants.CLASS_NAMES.length; i++) {
			String currentClass = dataPath + dataTypeFolder + File.separator + Constants.CLASS_NAMES[i] + File.separator
					+ extension + File.separator;
			appPaths.addAll(FileHandler.findFilesAsArray(currentClass, new String[] { extension }));

		}
		return appPaths;
	}

	private static double[] minIndexAndValue(ScorePair[] arr) {
		double minValue = Double.MAX_VALUE;
		int minIndex = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].getValue() < minValue) {
				minValue = arr[i].getValue();
				minIndex = i;
			}
		}
		return new double[] { (double) minIndex, minValue };
	}

	private static int indexOfMaxValue(Short[] values) {
		short maxValue = Short.MIN_VALUE;
		int maxIndex = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] > maxValue) {
				maxValue = values[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}
}

class ScorePair implements Comparable<ScorePair> {

	private String key;
	private double value;

	public ScorePair(String key, double value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public int compareTo(ScorePair item) {
		if (this.value < item.value) {
			return 1;
		} else if (this.value > item.value) {
			return -1;
		} else {
			return 0;
		}
	}
}