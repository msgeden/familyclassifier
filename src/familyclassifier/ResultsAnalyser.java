/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package familyclassifier;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

/**
 *
 * @author msgeden
 */
public class ResultsAnalyser {
//	public static void main(String[] args) {
//
//		for (int i = 0; i < Constants.CLASS_NAMES.length; i++)
//			generateHeatMapDataForSelectedFeatures(
//					"/Users/munirgeden/Desktop/data/report/json_distinctive_apicallwithargs_by_classwiseig.tsv", i);
//
//	}

	public static void generateHeatMapDataForSelectedFeatures(String frequencyFilePath, int classIndex) {
		// TODO Auto-generated method stub

		try {
			int size = 20;
			int indexX = 0;
			int indexY = 0;
			double interval = 1.0 / size;
			int[][] matrixCounts = new int[size][size];
			for (int i = 0; i < size; i++)
				for (int j = 0; j < size; j++)
					matrixCounts[i][j] = 0;

			File frequencyFile = new File(frequencyFilePath);
			
			String heatMapFilePath = frequencyFile.getParent() + File.separator + "heatmap" + File.separator
					+ frequencyFile.getName().replaceAll(".tsv","")+ "-" + Constants.CLASS_NAMES[classIndex] + "-heatmap-"
					 + ".tsv";
			File heatMap = new File(heatMapFilePath);
			FileUtils.deleteQuietly(heatMap);

			HashMap<String, Double[]> probs = new HashMap<String, Double[]>();

			Short[] countApps = new Short[] { 48, 47, 24, 39 };
			double[] classSizes = new double[] { 0, 0, 0, 0 };
			double[] otherClassSizes = new double[] { 0, 0, 0, 0 };
			double totalSize = 0.0;
			for (int i = 0; i < Constants.CLASS_NAMES.length; i++) {
				classSizes[i] = (double) countApps[i];
				totalSize += classSizes[i];

			}
			for (int i = 0; i < Constants.CLASS_NAMES.length; i++) {
				otherClassSizes[i] += totalSize - classSizes[i];
			}

			int tokenShift = 4;
			try {
				List<String> lines = FileUtils.readLines(frequencyFile, Charset.defaultCharset());
				int lineNumber = 0;
				for (String line : lines) {
					lineNumber++;
					// Skip header line
					if (line != null && !line.equals("") && lineNumber != 1) {
						String[] tokens = line.split(Constants.TAB_CHAR);
						Double probValues[] = new Double[] { 0.0, 0.0 };

						Short[] values = new Short[Constants.CLASS_NAMES.length];
						for (int i = 0; i < Constants.CLASS_NAMES.length; i++)
							values[i] = Short.parseShort(tokens[tokenShift + i]);

						double sumOfOthers = 0.0;
						for (int i = 0; i < Constants.CLASS_NAMES.length; i++)
							if (i != classIndex)
								sumOfOthers += (double) values[i].shortValue();

						probValues[0] = ((double) values[classIndex].shortValue() / classSizes[classIndex])
								- 0.00000001;
						probValues[1] = (sumOfOthers / otherClassSizes[classIndex]) - 0.00000001;

						probs.put(tokens[0], probValues);
					}
				}

				for (Map.Entry<String, Double[]> entry : probs.entrySet()) {

					// Calculate the number of apps for each classes that owns
					// the

					Double[] values = entry.getValue();
					indexX = (int) (values[0].doubleValue() / interval);
					indexY = (int) (values[1].doubleValue() / interval);
					matrixCounts[indexY][indexX]++;
				}
				double axisY = 0.0;
				System.out.print(axisY + Constants.TAB_CHAR);
				FileUtils.write(heatMap, axisY + Constants.TAB_CHAR, Charset.defaultCharset(), true);
				for (int i = 0; i < size; i++) {
					axisY += interval;
					System.out.print(String.format("%.3f", axisY) + Constants.TAB_CHAR);
					FileUtils.write(heatMap, String.format("%.3f", axisY) + Constants.TAB_CHAR,
							Charset.defaultCharset(), true);
				}
				System.out.print("\n");
				FileUtils.write(heatMap, "\n", Charset.defaultCharset(), true);
				for (int i = 0; i < size; i++) {
					System.out.print(String.format("%.3f", (i + 1) * interval) + Constants.TAB_CHAR);
					FileUtils.write(heatMap, String.format("%.3f", (i + 1) * interval) + Constants.TAB_CHAR,
							Charset.defaultCharset(), true);
					for (int j = 0; j < size; j++) {
						double logOccur = Math.log(matrixCounts[i][j]) / Math.log(2);
						//double logOccur = matrixCounts[i][j];
						if (Double.isInfinite(logOccur))
							logOccur = 0.0;
						System.out.print(matrixCounts[i][j] + ":" + logOccur + Constants.TAB_CHAR);
						FileUtils.write(heatMap, logOccur + Constants.TAB_CHAR, Charset.defaultCharset(), true);

					}
					System.out.print("\n");
					FileUtils.write(heatMap, "\n", Charset.defaultCharset(), true);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
