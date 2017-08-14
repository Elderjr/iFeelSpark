package crossValidation.iFeelCrossValidation;
import java.io.IOException;
import java.util.List;

import iFeel.Method;
import iFeel.MethodCreator;
import metrics.ConfusionMatrix;
import metrics.ConfusionMatrixSet;


public class Main {

	public static void main(String[] args) throws IOException{
		
		Method methods[] = { 
				//MethodCreator.getInstance().createMethod(Method.AFINN),
				//MethodCreator.getInstance().createMethod(Method.EMOLEX),
				//MethodCreator.getInstance().createMethod(Method.EMOTICONDS),
				//MethodCreator.getInstance().createMethod(Method.EMOTICONS),
				//MethodCreator.getInstance().createMethod(Method.HAPPINESS_INDEX),
				//MethodCreator.getInstance().createMethod(Method.MPQA),
				//MethodCreator.getInstance().createMethod(Method.OPINION),
				//MethodCreator.getInstance().createMethod(Method.PANAST),
				//MethodCreator.getInstance().createMethod(Method.SANN),
				//MethodCreator.getInstance().createMethod(Method.SASA),
				//MethodCreator.getInstance().createMethod(Method.SENTIC_NET),
				//MethodCreator.getInstance().createMethod(Method.SENTIMENT_140),
				MethodCreator.getInstance().createMethod(Method.SENTI_STRENGTH),
				//MethodCreator.getInstance().createMethod(Method.SO_CAL),
				//MethodCreator.getInstance().createMethod(Method.STANFORD),
				//MethodCreator.getInstance().createMethod(Method.SENTI_WORD_NET),
				//MethodCreator.getInstance().createMethod(Method.UMIGON),
				//MethodCreator.getInstance().createMethod(Method.VADER),
				//MethodCreator.getInstance().createMethod(Method.NRC_HashTag) 
			};
		
		String crossvalidationOutput = "/home/elderjr/Documents/RI/crossValidation(NonSupervisional)/";
		String dataSetPath = "/home/elderjr/Documents/RI/datasets/ufla.txt";
		String classificationPath = "/home/elderjr/Documents/RI/datasets/ufla_score.txt";
		//cria os arquivos de cross validation
		NonSupervisionalCrossValidation.createCrossValidationFiles(dataSetPath, classificationPath, crossvalidationOutput);
		System.out.println("Algoritmo comecou");
		for (Method method : methods) {
			ConfusionMatrixSet results = new ConfusionMatrixSet();
			for(int i = 0; i < 10; i++) {
				List<Integer> classifications = util.Utils.readClassifications(crossvalidationOutput + "test"+i+"_score.txt");
				List<Integer> predictions = method.analyseFile(crossvalidationOutput + "test"+i+".txt");
				results.addConfusionMatrix(ConfusionMatrix.create(predictions, classifications));
			}
			//results.printCrossValidationsMatrix();
			//results.printResults();
			results.printResultsLatex(method.getName());
			System.out.println();
		}
		

		
		
	}
}