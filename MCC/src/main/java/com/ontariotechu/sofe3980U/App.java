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
		double[] CE = { Double.NaN, 0.0, 0.0, 0.0, 0.0 };// ignore first index, 1-based
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
		// Use StringBuilder for output
		final int FIELD_WIDTH = 8; // Adjustable width for each column
		StringBuilder sb = new StringBuilder();

		// Cross-Entropy line
		sb.append(String.format("CE = %.4f%n", -ceSum / count));
		sb.append("Confusion Matrix:\n");

		// Header row (y^=1, y^=2, etc.)
		for (int i = 0; i < 12; i++) { // 12 spaces for indentation
			sb.append(" ");
		}
		for (int c = 1; c < SIZE; c++) { // Up to SIZE-1 (excluding unused index)
			String header = "y^=" + c;
			sb.append(header);
			for (int j = header.length(); j < FIELD_WIDTH; j++) { // Pad right (left-align)
				sb.append(" ");
			}
		}
		sb.append("\n");

		// Matrix rows
		for (int r = 1; r < SIZE; r++) {
			String rowLabel = "y=" + r;
			sb.append(rowLabel);
			for (int j = rowLabel.length(); j < 12; j++) { // Pad right to 12 spaces
				sb.append(" ");
			}
			for (int c = 1; c < SIZE; c++) {
				String value = String.valueOf(matrix[r][c]);
				for (int j = value.length(); j < FIELD_WIDTH; j++) { // Pad left (right-align)
					sb.append(" ");
				}
				sb.append(value);
			}
			sb.append("\n");
		}

		// Print the entire output at once
		System.out.print(sb.toString());
	}
}
