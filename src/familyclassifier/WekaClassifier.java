/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package familyclassifier;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import weka.core.Instances;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.lazy.IBk;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.functions.NeuralNetwork;
import weka.classifiers.functions.SMO;
import weka.filters.unsupervised.attribute.Remove;

import org.apache.commons.io.FileUtils;

/**
 *
 * @author msgeden
 */
public class WekaClassifier {

	public static void getClassifierResultsForAll(String featureType) {
		try {
			String extension = FileHandler.readConfigValue(Constants.FILE_EXTENSION_CONFIG);
			Date date = new Date();
			String modifiedDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
			String resultTablePath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG) + "all_" + extension
					+ Constants.UNDERSCORE + featureType + Constants.UNDERSCORE + "weka_total_" + modifiedDate + ".tsv";
			File results = new File(resultTablePath);

			String[] inputSizes = FileHandler.readConfigValue(Constants.NUMBER_OF_DATA_INPUT_CONFIG).split(",");

			for (int i = 0; i < Constants.EXTRACTION_LABELS.length; i++) {
				FileUtils.write(results, "\nExtension\tExtraction\tFeature\tTimestamp", Charset.defaultCharset(), true);
				FileUtils.write(results, "\n" + extension + "\t" + Constants.EXTRACTION_LABELS[i] + "\t" + featureType
						+ "\t" + new Timestamp(new Date().getTime()), Charset.defaultCharset(), true);
//				FileUtils.write(results,
//						"\nInput Size\tNB\tROC(M)\tTPR(M)\tFPR(M)\tKNN("
//								+ FileHandler.readConfigValue(Constants.KNN_KVALUE_CONFIG)
//								+ ")\tROC(M)\tTPR(M)\tFPR(M)\tJ48\tROC(M)\tTPR(M)\tFPR(M)\tRF\tROC(M)\tTPR(M)\tFPR(M)\tSVM\tROC(M)\tTPR(M)\tFPR(M)\tNN",
//						Charset.defaultCharset(), true);
				FileUtils.write(results,
						"\nInput Size\tNB\tKNN("
								+ FileHandler.readConfigValue(Constants.KNN_KVALUE_CONFIG)
								+ ")\tRF\tSVM\tNN",
						Charset.defaultCharset(), true);
				
				for (String inputSize : inputSizes) {
					FileUtils.write(results, "\n" + inputSize, Charset.defaultCharset(), true);
					String trainingDataPath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)
							+ Constants.TRAIN_LABEL + File.separator + "train" + Constants.UNDERSCORE + extension
							+ Constants.UNDERSCORE + featureType + Constants.UNDERSCORE + inputSize
							+ Constants.UNDERSCORE + i + ".arff";

					String testDataPath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)
							+ Constants.TEST_LABEL + File.separator + "test" + Constants.UNDERSCORE + extension
							+ Constants.UNDERSCORE + featureType + Constants.UNDERSCORE + inputSize
							+ Constants.UNDERSCORE + i + ".arff";

				
					String[] nb = getClassifierResults(trainingDataPath, testDataPath, extension, "nb", featureType);
					FileUtils.write(results, "\t" + nb[0],
							Charset.defaultCharset(), true);

					String[] knn = getClassifierResults(trainingDataPath, testDataPath, extension, "knn", featureType);
					FileUtils.write(results, "\t" + knn[0],
							Charset.defaultCharset(), true);

					String[] rf = getClassifierResults(trainingDataPath, testDataPath, extension, "rf", featureType);
					FileUtils.write(results, "\t" + rf[0],
							Charset.defaultCharset(), true);

					String[] svm = getClassifierResults(trainingDataPath, testDataPath, extension, "svm", featureType);
					FileUtils.write(results, "\t" + svm[0],
							Charset.defaultCharset(), true);
					
					String[] nn = getClassifierResults(trainingDataPath, testDataPath, extension, "nn", featureType);
					FileUtils.write(results, "\t" + nn[0],
							Charset.defaultCharset(), true);
					
					
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String[] getClassifierResults(String trainingDataPath, String testDataPath, String extension,
			String algorithm, String featureType) {
		String[] response = new String[] { "", "", "", "" };

		try {
			if (algorithm.equals("all")) {
				getClassifierResults(trainingDataPath, testDataPath, extension, "mnb", featureType);
				getClassifierResults(trainingDataPath, testDataPath, extension, "nb", featureType);
				getClassifierResults(trainingDataPath, testDataPath, extension, "knn", featureType);
				getClassifierResults(trainingDataPath, testDataPath, extension, "j48", featureType);
				getClassifierResults(trainingDataPath, testDataPath, extension, "rf", featureType);
				getClassifierResults(trainingDataPath, testDataPath, extension, "svm", featureType);
				getClassifierResults(trainingDataPath, testDataPath, extension, "nn", featureType);

			} else {

				String resultsPath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)
						+ File.separator + "weka"+ File.separator + ("weka" + Constants.UNDERSCORE + algorithm + Constants.UNDERSCORE + extension
								+ Constants.UNDERSCORE + featureType + ".log").toLowerCase();

				File resultsFile = new File(resultsPath);
				// FileUtils.deleteQuietly(resultsFile);

				// Training Instances
				DataSource trainingSource = new DataSource(trainingDataPath);
				Instances trainingData = trainingSource.getDataSet();
				if (trainingData.classIndex() == -1) {
					trainingData.setClassIndex(trainingData.numAttributes() - 1);
				}

				// Test Instances
				DataSource testSource = new DataSource(testDataPath);
				Instances testData = testSource.getDataSet();
				if (testData.classIndex() == -1) {
					testData.setClassIndex(testData.numAttributes() - 1);
				}
				int numberOfFeatures = trainingData.numAttributes();
				Remove rm = new Remove();
				if (trainingData.attribute(0).name().equals("appname")) {
					numberOfFeatures -= 1;
					rm.setAttributeIndices("1"); // remove 1st attribute since
					// it is
				}
				// id
				FilteredClassifier fc = new FilteredClassifier();
				fc.setFilter(rm);
				if (algorithm.equals("j48")) {
					J48 j48 = new J48();
					j48.setUnpruned(true);
					fc.setClassifier(j48);
				} else if (algorithm.equals("nb")) {
					NaiveBayes nb = new NaiveBayes();
					fc.setClassifier(nb);
				} else if (algorithm.equals("mnb")) {
					NaiveBayesMultinomial nb = new NaiveBayesMultinomial();
					fc.setClassifier(nb);
				} else if (algorithm.equals("knn")) {
					IBk ibk = new IBk();
					ibk.setKNN(Integer.parseInt(FileHandler.readConfigValue((Constants.KNN_KVALUE_CONFIG))));
					fc.setClassifier(ibk);
				} else if (algorithm.equals("rf")) {
					RandomForest rf = new RandomForest();
					fc.setClassifier(rf);
				} else if (algorithm.equals("svm")) {
					SMO svm = new SMO();
					svm.setBuildCalibrationModels(true);
					fc.setClassifier(svm);
				} else if (algorithm.equals("nn")) {
					NeuralNetwork nn = new NeuralNetwork();
					fc.setClassifier(nn);
				}
				// train and make predictions
				fc.buildClassifier(trainingData);

				// evaluate classifier and print some statistics
				// String[] evalOptions = new String[2];
				// evalOptions[0] = "-t";
				// evalOptions[1] = "/some/where/somefile.arff";

				Evaluation eval = new Evaluation(trainingData);
				eval.evaluateModel(fc, testData);

				for (int i=0;i<Constants.CLASS_NAMES.length;i++)
				{
					ThresholdCurve tc = new ThresholdCurve();
					int classIndex = i;
					Instances result = tc.getCurve(eval.predictions(), classIndex);
	
					CSVSaver csvSaver = new CSVSaver();
					csvSaver.setInstances(result);
					String fileName = new File(trainingDataPath).getName();
					fileName = fileName.substring(fileName.indexOf(Constants.UNDERSCORE) + 1, fileName.lastIndexOf("."));
					String rocData = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)
							+ File.separator + "weka"+ File.separator + (fileName + Constants.UNDERSCORE + algorithm + "-" + Constants.CLASS_NAMES[i] + ".csv").toLowerCase();
	
					csvSaver.setFile(new File(rocData));
					csvSaver.writeBatch();
				}
				response[0] = String.format("%.2f", eval.pctCorrect());
				//response[1] = String.format("%.4f", eval.areaUnderROC(1));
				//response[2] = String.format("%.4f", eval.truePositiveRate(1));
				//response[3] = String.format("%.4f", eval.falsePositiveRate(1));
				System.out.println(eval.toSummaryString("\nSummary Results: " + algorithm.toUpperCase() + ","
						+ (numberOfFeatures - 1) + "\n============\n", false));
				System.out.println(eval.toClassDetailsString("\nClass Details\n============\n"));
				System.out.println(eval.toMatrixString("\nMatrix Results\n============\n"));
				System.out.println("\nApps\n======\n");

				FileUtils
						.write(resultsFile,
								eval.toSummaryString("\nSummary Results: " + algorithm.toUpperCase() + ","
										+ (numberOfFeatures - 1) + "\n============\n", false),
								Charset.defaultCharset(), true);
				FileUtils.write(resultsFile, eval.toClassDetailsString("\nClass Details\n============\n"),
						Charset.defaultCharset(), true);
				FileUtils.write(resultsFile, eval.toMatrixString("\nMatrix Results\n============\n"),
						Charset.defaultCharset(), true);
				// FileUtils.write(resultsFile, "\nApps\n======\n", true);
				//
				// for (int i = 0; i < testData.numInstances(); i++) {
				// double pred = fc.classifyInstance(testData.instance(i));
				// System.out.print("Name: "
				// + testData.instance(i).stringValue(0));
				// FileUtils.write(resultsFile, "Name: "
				// + testData.instance(i).stringValue(0), true);
				// System.out.print(", Actual: "
				// + testData.classAttribute().value(
				// (int) testData.instance(i).classValue()));
				// FileUtils.write(
				// resultsFile,
				// ", Actual: "
				// + testData.classAttribute().value(
				// (int) testData.instance(i)
				// .classValue()), true);
				// System.out.println(", Predicted: "
				// + testData.classAttribute().value((int) pred));
				// FileUtils.write(resultsFile, ", Predicted: "
				// + testData.classAttribute().value((int) pred)
				// + "\n", true);
				// }
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
}
