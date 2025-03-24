package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Evaluate Single Variable Continuous Regression
 *
 */
public class App {
	private static final String DIR = System.getProperty("user.dir") + "\\SVCR\\model_";

	public static void main(String[] args) {
		// Arrays to store metrics for each model (1-based indexing: [0] unused, [1-3]
		// for models)
		double[] maeResults = new double[4];
		double[] mseResults = new double[4];
		double[] mareResults = new double[4];
		int numModels = 3;

		// Process each model file
		for (int i = 1; i <= numModels; i++) {
			String filePath = DIR + i + ".csv";
			List<String[]> allData;

			// Read CSV file
			try (FileReader filereader = new FileReader(filePath);
					CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build()) {
				allData = csvReader.readAll();
			} catch (Exception e) {
				System.out.println("Error reading the CSV file " + filePath + ": " + e);
				return;
			}

			// Variables for this model
			int count = 0;
			double maeSum = 0.0;
			double mseSum = 0.0;
			double mareSum = 0.0;

			// Calculate metrics for each row
			for (String[] row : allData) {
				double yTrue = Double.parseDouble(row[0]);
				double yPred = Double.parseDouble(row[1]);
				count++;

				double diff = Math.abs(yTrue - yPred);
				maeSum += diff;
				mseSum += diff * diff; // Optimized: Avoid Math.pow() for squaring
				mareSum += (diff / (Math.abs(yTrue) + Double.MIN_VALUE)) * 100.0;
			}

			// Compute averages and store results
			maeResults[i] = maeSum / count;
			mseResults[i] = mseSum / count;
			mareResults[i] = mareSum / count;

			// Print using StringBuilder
			StringBuilder sb = new StringBuilder();
			sb.append("for model_").append(i).append(".csv\n");
			sb.append("        MSE = ").append(Double.toString(mseResults[i])).append("\n");
			sb.append("        MAE = ").append(Double.toString(maeResults[i])).append("\n");
			sb.append("        MARE = ").append(Double.toString(mareResults[i])).append("\n");
			System.out.print(sb.toString());
		}

		// Determine the best model for each metric
		int bestMseModel = 1;
		int bestMaeModel = 1;
		int bestMareModel = 1;

		for (int i = 2; i <= numModels; i++) {
			if (mseResults[i] < mseResults[bestMseModel])
				bestMseModel = i;
			if (maeResults[i] < maeResults[bestMaeModel])
				bestMaeModel = i;
			if (mareResults[i] < mareResults[bestMareModel])
				bestMareModel = i;
		}

		// Print recommendations
		StringBuilder sb = new StringBuilder();
        sb.append("According to MSE, The best model is model_").append(bestMseModel).append(".csv\n");
        sb.append("According to MAE, The best model is model_").append(bestMaeModel).append(".csv\n");
        sb.append("According to MARE, The best model is model_").append(bestMareModel).append(".csv\n");
        System.out.print(sb.toString());
	}
}