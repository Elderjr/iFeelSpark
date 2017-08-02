package mpqa4lg.opin.supervised;

import weka.core.*;
import weka.classifiers.bayes.*;
import weka.classifiers.*;
import java.util.*;
import java.io.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;

public class SupervisedClassifier
{
    public static void main(final String[] args) {
        boolean cv = false;
        File dataFile = null;
        File trainFile = null;
        File testFile = null;
        if (args.length == 0) {
            System.err.println("Usage: java SupervisedClassifier trainData (testData)");
            System.exit(-1);
        }
        else if (args.length == 1) {
            cv = true;
            dataFile = new File(args[0]);
            if (!dataFile.exists()) {
                System.err.println("Data File does not exist!");
                System.exit(-1);
            }
        }
        else {
            trainFile = new File(args[0]);
            if (!trainFile.exists()) {
                System.err.println("Train File does not exist!");
                System.exit(-1);
            }
            testFile = new File(args[1]);
            if (!testFile.exists()) {
                System.err.println("Test File does not exist!");
                System.exit(-1);
            }
        }
        try {
            if (cv) {
                final FileReader reader = new FileReader(dataFile);
                final Instances data = new Instances((Reader)reader);
                data.setClassIndex(data.numAttributes() - 1);
                reader.close();
                final NaiveBayes naive = new NaiveBayes();
                naive.setUseSupervisedDiscretization(true);
                final Evaluation eval = new Evaluation(data);
                final ArrayList<Double> predictions = crossValidate(eval, (Classifier)naive, data, 10, new Random(0L));
                System.out.println(eval.toSummaryString(true));
                System.out.println("\n\n\n\n");
                System.out.println(eval.toClassDetailsString());
                writeEvaluation(new File("cv_eval.txt"), eval, predictions, data);
            }
            else {
                FileReader reader = new FileReader(trainFile);
                final Instances train = new Instances((Reader)reader);
                train.setClassIndex(train.numAttributes() - 1);
                reader.close();
                reader = new FileReader(testFile);
                final Instances test = new Instances((Reader)reader);
                test.setClassIndex(test.numAttributes() - 1);
                reader.close();
                final Instances filteredTrain = filterAttribute(train, "first");
                final Instances filteredTest = filterAttribute(test, "first");
                final NaiveBayes naive2 = new NaiveBayes();
                naive2.setUseSupervisedDiscretization(true);
                final Evaluation eval = new Evaluation(filteredTrain);
                final ArrayList<Double> predictions = validate(eval, (Classifier)naive2, filteredTrain, filteredTest);
                System.out.println(eval.toSummaryString(true));
                System.out.println("\n\n\n\n");
                System.out.println(eval.toClassDetailsString());
                writeEvaluation(new File("eval.txt"), eval, predictions, test);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private static void writeEvaluation(final File file, final Evaluation eval, final ArrayList<Double> predictions, final Instances data) throws Exception {
        FileOutputStream rawFile = null;
        OutputStreamWriter sWriter = null;
        BufferedWriter bWriter = null;
        try {
            rawFile = new FileOutputStream(file);
            sWriter = new OutputStreamWriter(rawFile, "UTF-8");
            bWriter = new BufferedWriter(sWriter, 20480);
            bWriter.append((CharSequence)"************************************************************");
            bWriter.newLine();
            bWriter.append((CharSequence)eval.toSummaryString(true));
            bWriter.append((CharSequence)"\n\n\n\n");
            bWriter.append((CharSequence)eval.toClassDetailsString());
            bWriter.append((CharSequence)"\n\n\n\n");
            bWriter.append((CharSequence)eval.toMatrixString());
            bWriter.append((CharSequence)"\n\n\n\n");
            int subjOK = 0;
            int subjFalse = 0;
            for (int i = 0; i < predictions.size(); ++i) {
                bWriter.append((CharSequence)(String.valueOf(data.attribute(0).value((int)data.instance(i).value(0))) + "\tpredicted: " + data.classAttribute().value((int)(Object)predictions.get(i)) + "\tactual: " + data.classAttribute().value((int)data.instance(i).classValue())));
                bWriter.newLine();
                if ((int)(Object)predictions.get(i) == 0 && data.instance(i).classValue() == (int)(Object)predictions.get(i)) {
                    ++subjOK;
                }
                else if ((int)(Object)predictions.get(i) == 0 && data.instance(i).classValue() != (int)(Object)predictions.get(i)) {
                    ++subjFalse;
                }
            }
            bWriter.flush();
            System.out.println(subjOK);
            System.out.println(subjFalse);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }
        catch (IOException e3) {
            e3.printStackTrace();
        }
        finally {
            try {
                if (rawFile != null) {
                    rawFile.close();
                }
                if (sWriter != null) {
                    sWriter.close();
                }
                if (bWriter != null) {
                    bWriter.close();
                }
            }
            catch (IOException e4) {
                e4.printStackTrace();
            }
        }
        try {
            if (rawFile != null) {
                rawFile.close();
            }
            if (sWriter != null) {
                sWriter.close();
            }
            if (bWriter != null) {
                bWriter.close();
            }
        }
        catch (IOException e4) {
            e4.printStackTrace();
        }
    }
    
    public static Instances filterAttribute(final Instances data, final String range) throws Exception {
        final Remove idRemover = new Remove();
        idRemover.setAttributeIndices(range);
        idRemover.setInputFormat(data);
        final Instances filteredData = Filter.useFilter(data, (Filter)idRemover);
        return filteredData;
    }
    
    public static ArrayList<Double> crossValidate(final Evaluation eval, final Classifier classifier, final Instances data, final int numFolds, final Random random) throws Exception {
        final ArrayList<Double> output = new ArrayList<Double>();
        data.randomize(random);
        if (data.classAttribute().isNominal()) {
            data.stratify(numFolds);
        }
        for (int i = 0; i < numFolds; ++i) {
            Instances train = data.trainCV(numFolds, i, random);
            train = filterAttribute(train, "first");
            eval.setPriors(train);
            final Classifier copiedClassifier = Classifier.makeCopy(classifier);
            copiedClassifier.buildClassifier(train);
            Instances test = data.testCV(numFolds, i);
            test = filterAttribute(test, "first");
            final double[] predictions = eval.evaluateModel(copiedClassifier, test, new Object[0]);
            double[] array;
            for (int length = (array = predictions).length, j = 0; j < length; ++j) {
                final double d = array[j];
                output.add(d);
            }
        }
        return output;
    }
    
    public static ArrayList<Double> validate(final Evaluation eval, final Classifier classifier, final Instances train, final Instances test) throws Exception {
        final ArrayList<Double> output = new ArrayList<Double>();
        classifier.buildClassifier(train);
        final double[] predictions = eval.evaluateModel(classifier, test, new Object[0]);
        double[] array;
        for (int length = (array = predictions).length, i = 0; i < length; ++i) {
            final double d = array[i];
            output.add(d);
        }
        return output;
    }
}
