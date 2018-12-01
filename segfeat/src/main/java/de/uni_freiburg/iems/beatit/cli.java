package de.uni_freiburg.iems.beatit;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.concurrent.Callable;

import picocli.CommandLine;

@CommandLine.Command(
    description = "Prints mean and variance of a CSV file",
    name = "segfeat",
    mixinStandardHelpOptions = true,
    version = "segfeat 1.0")

public class cli implements Callable<Void> {

    private static final String SEPARATOR = " ";

    @CommandLine.Parameters(index = "0", description = "file to read from")
    private File file = new File("-");

    @CommandLine.Option(names = {"-n", "--duration"}, description = "number of samples to merge")
    private Integer windowlength = 100;

    public static void main(String[] args) {
        CommandLine.call(new cli(), args);
    }

    @Override
    public Void call() throws Exception {
        BufferedReader in;

        if ("-".equals(file.getName()))
            in = new BufferedReader(new InputStreamReader(System.in));
        else
            in = new BufferedReader(new FileReader(file));

        SegFeat segFeat = null;
        double array[] = null;
        CSVReader csv = new CSVReaderBuilder(in)
                . withCSVParser(
                  new CSVParserBuilder()
                . withSeparator(' ')
                . withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                . build())
                .build();
        boolean hasLabels = false;

        for (String[] line = csv.readNext();
                      line != null;
                      line = csv.readNext()) {

            if (line.length == 0 || line[0].trim().startsWith("#"))
                continue;

            String[] fields = Arrays.stream(line).filter(f -> f != null).toArray(String[]::new);

            if (array == null) {
                try {
                    Double.parseDouble(fields[0]);

                    segFeat = new SegFeat.Builder()
                            .setWindowSize(100)
                            .setSampleSize(line.length)
                            .build();
                    array = new double[line.length];
                } catch (NumberFormatException e) {
                    hasLabels = true;
                    segFeat = new SegFeat.Builder()
                            .setWindowSize(100)
                            .setSampleSize(fields.length-1)
                            .build();
                    array = new double[fields.length-1];
                }

            }

            for (int i=0; i<array.length; i++)
                array[i] = Double.parseDouble(fields[i + (hasLabels ? 1 : 0)]);

            segFeat.write(hasLabels ? fields[0] : null, array);

            SegFeat.FeatureVector featureVector = segFeat.read();
            if (featureVector != null) {
                System.out.print(featureVector.mLabel);
                System.out.print(SEPARATOR);
                System.out.println(dblarr2str(featureVector.mVector));
            }
        }

        return null;
    }

    private String dblarr2str(double[] featureVector) {
        StringJoiner sj = new StringJoiner(SEPARATOR);

        for (double feature : featureVector)
            sj.add(Double.toString(feature));

        return sj.toString();
    }
}
