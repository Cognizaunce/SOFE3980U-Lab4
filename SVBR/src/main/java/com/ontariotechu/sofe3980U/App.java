package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.*;

/**
 * Evaluate Single Variable Continuous Regression
 *
 */
public class App {
	public static void main(String[] args) {
		String filePath = "SVBR\\model_1.csv";
		FileReader filereader;
		List<String[]> allData;
		try {
			filereader = new FileReader(filePath);
			CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
			allData = csvReader.readAll();
		} catch (Exception e) {
			System.out.println("Error reading the CSV file");
			return;
		}

		int count = 0;
		for (String[] row : allData) {
			int y_true = Integer.parseInt(row[0]);
			float y_predicted = Float.parseFloat(row[1]);
			System.out.print(y_true + "  \t  " + y_predicted);
			System.out.println();
			count++;
			if (count == 10) {
				break;
			}
		}

	}

	static double calculateAUC(List<Integer> dataTrue, List<Float> dataEstimate) {
		double[] y = new double[100];
		double[] x = new double[100];
		int num = dataTrue.size();
		int numPositive = 0, numNegative = 0;

		// Count n +ve and n -ve
		for (int i = 0; i < num; i++) {
			if (dataTrue.get(i) == 1)
				numPositive++;
			else if (dataTrue.get(i) == 0)
				numNegative++;
		}
		// Set x and y values for ROC
		double th = 0, TP = 0, FP = 0;
		for (int i = 0; i < 100; i++) {
			// Get threshold
			th = i / 100;
			// Using new threshold count true positives and false positives
			for (int j = 0; j < num; j++) {
				if (dataEstimate.get(j) >= th) {
					if (dataTrue.get(j) == 1)
						TP++;
					else if (dataTrue.get(j) == 0)
						FP++;
					else
						System.err.println("Bad data!");
				}
			}
			y[i] = TP / numPositive;
			x[i] = FP / numNegative;
		}
		// AUC
		double auc = 0;
		for (int i = 1; i < 100; i++)
			auc += ((y[i - 1] + y[i]) * Math.abs(x[i - 1] - x[i])) / 2.0;

		return auc;
	}
}