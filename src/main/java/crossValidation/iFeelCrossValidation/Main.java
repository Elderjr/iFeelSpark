package crossValidation.iFeelCrossValidation;
import java.io.IOException;
import java.util.List;

import iFeel.Method;
import iFeel.MethodCreator;
import metrics.ConfusionMatrix;
import metrics.ConfusionMatrixSet;


public class Main {

	public static void main(String[] args) throws IOException{
		Method method = MethodCreator.getInstance().createMethod(Method.HAPPINESS_INDEX);
		String crossvalidationOutput = "/home/elderjr/Documents/RI/crossValidation(NonSupervisional)/";
		String dataSetPath = "/home/elderjr/Documents/RI/datasets/stanford_tweets.txt";
		String classificationPath = "/home/elderjr/Documents/RI/datasets/stanford_tweets_so_score.txt";
		NonSupervisionalCrossValidation.createCrossValidationFiles(dataSetPath, classificationPath, crossvalidationOutput);
		ConfusionMatrixSet results = new ConfusionMatrixSet();
		for(int i = 0; i < 10; i++) {
			List<Integer> classifications = util.Utils.readClassifications(crossvalidationOutput + "test"+i+"_score.txt");
			List<Integer> predictions = method.analyseFile(crossvalidationOutput + "test"+i+".txt");
			results.addConfusionMatrix(ConfusionMatrix.create(predictions, classifications));
		}
		results.printResults();
	}
}