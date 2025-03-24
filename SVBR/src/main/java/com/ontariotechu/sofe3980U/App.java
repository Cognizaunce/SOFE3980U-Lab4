package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Evaluate Single Variable Continuous Regression
 *
 */
public class App {
    private static final String DIR = System.getProperty("user.dir") + "\\SVBR\\model_";

    public static void main(String[] args) {
        // Arrays to store metrics for each model (1-based indexing: [0] unused, [1-3]
        // for models)
        double[] bceResults = new double[4];
        double[] accResults = new double[4];
        double[] prcnResults = new double[4];
        double[] reclResults = new double[4];
        double[] f1Results = new double[4];
        double[] aucResults = new double[4]; // AUC-ROC
        int numModels = 3;

        // Process each model file via try with resources
        for (int i = 1; i <= numModels; i++) {
            String filePath = DIR + i + ".csv";
            List<String[]> allData;

            // Read CSV file
            try (FileReader filereader = new FileReader(filePath);
                    CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build()) {
                allData = csvReader.readAll();
            } catch (Exception e) {
                System.out.println("Error reading the CSV file");
                return;
            }

            // Variables for this model
            int count = 0;
            int tp = 0, fp = 0, tn = 0, fn = 0;
            double bceSum = 0.0;

            // Temporary lists for AUC, scoped to this model
            List<Integer> labels = new ArrayList<>();
            List<Double> predictions = new ArrayList<>();

            // Calculate metrics for each row
            for (String[] row : allData) {
                int y_true = Integer.parseInt(row[0]);
                double y_predicted = Double.parseDouble(row[1]);
                count++;

                // If label is true
                if (y_true == 1) {
                    if (y_predicted != 0.0)
                        bceSum += Math.log(y_predicted);

                    if (y_predicted > 0.5)
                        tp++;
                    else
                        fn++;
                }
                // If label is false
                else if (y_true == 0) {
                    if (y_predicted < 1.0)
                        bceSum += Math.log(1.0 - y_predicted);

                    if (y_predicted > 0.5)
                        fp++;
                    else
                        tn++;
                    // If invalid label
                } else {
                    System.out.println("Invalid label in model_" + i + ".csv: " + y_true);
                    return;
                }
                labels.add(y_true);
                predictions.add(y_predicted);

            }
            // Calculate Results
            bceResults[i] = -bceSum / count;
            accResults[i] = (tp + tn) / (double) count;
            prcnResults[i] = (tp + fp == 0) ? 0.0 : (double) tp / (tp + fp);
            reclResults[i] = (tp + fn == 0) ? 0.0 : (double) tp / (tp + fn);
            f1Results[i] = (prcnResults[i] + reclResults[i] == 0) ? 0.0
                    : 2.0 * (prcnResults[i] * reclResults[i]) / (prcnResults[i] + reclResults[i]);
            aucResults[i] = calculateAUC(labels, predictions);

            // Print Results
            StringBuilder sb = new StringBuilder();
            sb.append("For model_").append(i).append(".csv\n");
            sb.append("        BCE = ").append(Double.toString(bceResults[i])).append("\n");
            sb.append("        Confusion Matrix\n");
            sb.append("                        y=1      y=0\n");
            sb.append("                y^=1    ").append(tp).append("    ").append(fp).append("\n");
            sb.append("                y^=0    ").append(fn).append("    ").append(tn).append("\n");
            sb.append("        Accuracy = ").append(Double.toString(accResults[i])).append("\n");
            sb.append("        Precision = ").append(Double.toString(prcnResults[i])).append("\n");
            sb.append("        Recall = ").append(Double.toString(reclResults[i])).append("\n");
            sb.append("        F1 Score = ").append(Double.toString(f1Results[i])).append("\n");
            sb.append("        AUC-ROC = ").append(Double.toString(aucResults[i])).append("\n");
            System.out.print(sb.toString());
        }
        int bestBceModel = 1, bestAccModel = 1, bestPrcnModel = 1;
        int bestReclModel = 1, bestF1Model = 1, bestAucModel = 1;
        for (int i = 2; i <= numModels; i++) {
            if (bceResults[i] < bceResults[bestBceModel])
                bestBceModel = i;
            if (accResults[i] > accResults[bestAccModel])
                bestAccModel = i;
            if (prcnResults[i] > prcnResults[bestPrcnModel])
                bestPrcnModel = i;
            if (reclResults[i] > reclResults[bestReclModel])
                bestReclModel = i;
            if (f1Results[i] > f1Results[bestF1Model])
                bestF1Model = i;
            if (aucResults[i] > aucResults[bestAucModel])
                bestAucModel = i;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("According to BCE, The best model is model_").append(bestBceModel).append(".csv\n");
        sb.append("According to Accuracy, The best model is model_").append(bestAccModel).append(".csv\n");
        sb.append("According to Precision, The best model is model_").append(bestPrcnModel).append(".csv\n");
        sb.append("According to Recall, The best model is model_").append(bestReclModel).append(".csv\n");
        sb.append("According to F1 Score, The best model is model_").append(bestF1Model).append(".csv\n");
        sb.append("According to AUC-ROC, The best model is model_").append(bestAucModel).append(".csv\n");
        System.out.print(sb.toString());
    }

    static double calculateAUC(List<Integer> dataTrue, List<Double> dataEstimate) {
        int num = dataTrue.size();
        if (num != dataEstimate.size() || num == 0)
            return Double.NaN;

        // Count positives and negatives
        int numPositive = 0, numNegative = 0;
        for (int i = 0; i < num; i++) {
            int label = dataTrue.get(i);
            if (label == 1)
                numPositive++;
            else if (label == 0)
                numNegative++;
            else
                return Double.NaN; // Invalid label
        }
        if (numPositive == 0 || numNegative == 0)
            return Double.NaN; // Cannot compute AUC

        // Pair true labels with estimates and sort by estimate (descending)
        List<double[]> pairs = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            pairs.add(new double[] { dataEstimate.get(i), dataTrue.get(i) });
        }
        pairs.sort((a, b) -> Double.compare(b[0], a[0])); // Sort descending by estimate

        // Compute ROC points in one pass
        double[] x = new double[num + 1]; // FPR: FP / numNegative
        double[] y = new double[num + 1]; // TPR: TP / numPositive
        double TP = 0, FP = 0;
        double invPos = 1.0 / numPositive, invNeg = 1.0 / numNegative;
        int idx = 0;

        x[0] = 0;
        y[0] = 0; // Origin point
        for (int i = 0; i < num; i++) {
            double estimate = pairs.get(i)[0];
            int label = (int) pairs.get(i)[1];
            if (label == 1)
                TP++;
            else if (label == 0)
                FP++;

            // Only add point if estimate changes (new threshold)
            if (i == num - 1 || estimate != pairs.get(i + 1)[0]) {
                idx++;
                y[idx] = TP * invPos;
                x[idx] = FP * invNeg;
            }
        }

        // Trapezoidal rule for AUC
        double auc = 0;
        for (int i = 1; i <= idx; i++) {
            auc += ((y[i - 1] + y[i]) * Math.abs(x[i] - x[i - 1])) * 0.5;
        }

        return auc;
    }
}