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
	public static void main(String[] args) {
		List<String[]> allData;
		try (FileReader filereader = new FileReader("MCC\\model.csv");
				CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build()) {
			allData = csvReader.readAll();
		} catch (Exception e) {
			System.out.println("Error reading the CSV file: " + e);
			return;
		}

		int count = 0;
		double ceSum = 0.0; // Placeholder for CrossEntropy Calculation
		final int SIZE = 6; // Constant for matrix size
		int[][] matrix = new int[SIZE][SIZE]; // 6x6 confusion matrix, 0 index unused

		for (String[] row : allData) {
			int y_true = Integer.parseInt(row[0]);
			if (y_true >= 1 && y_true < SIZE) { // Valid range: 1 to 5
				double prediction = 0.0;
				int y_pred = 1; // Default to first valid class (1)

				// Find max prediction and corresponding y_pred
				for (int x = 1; x < SIZE; x++) {
					double temp = Double.parseDouble(row[x]);
					if (temp > prediction) {
						prediction = temp;
						y_pred = x;
					}
				}
				matrix[y_true][y_pred]++;
			} else {
				System.out.println("Invalid label in CSV file" + y_true);
				return;
			}
			// Update confusion matrix
			count++; // Increment count of valid predictions
			ceSum += Math.log(Double.parseDouble(row[y_true]));
		}
		// Printing logic:
		StringBuilder sb = new StringBuilder();

		// Append CE value
		sb.append("CE = ").append(-ceSum / count).append("\n");
		sb.append("Confusion Matrix:\n");

		// Header row
		sb.append(String.format("%9s", ""));
		for (int c = 1; c < SIZE; c++) {
			sb.append(String.format("y^=%-4d", c));
		}
		sb.append("\n");

		// Matrix rows
		for (int r = 1; r < SIZE; r++) {
			sb.append(String.format("%3s y=%d%3s", "", r, ""));
			for (int c = 1; c < SIZE; c++) {
				sb.append(String.format("%-7d", matrix[r][c]));
			}
			sb.append("\n");
		}

		// Print all at once to minimize I/O overhead
		System.out.print(sb.toString());
	}
}
