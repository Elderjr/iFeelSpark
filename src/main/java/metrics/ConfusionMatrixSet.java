package metrics;

import java.util.ArrayList;


public class ConfusionMatrixSet {

	private ArrayList<ConfusionMatrix> confusionMatrixs;
	private double meanAcc = 0.0;
	private double meanMacroF1 = 0.0;
	private double meanPrecision[] = {0.0, 0.0, 0.0};
	private double meanRecall[] = {0.0, 0.0, 0.0};
	
	public ConfusionMatrixSet(){
		this.confusionMatrixs = new ArrayList<>();
	}
	
	public void addConfusionMatrix(ConfusionMatrix confusionMatrix){
		this.confusionMatrixs.add(confusionMatrix);
	}
	
	public ArrayList<ConfusionMatrix> getConfusionMatrixs(){
		return this.confusionMatrixs;
	}
	
	public void calculeMeans(){
		for(ConfusionMatrix matrix : this.confusionMatrixs){
			this.meanAcc += matrix.getAccuracy();
			this.meanMacroF1 += matrix.getMacroF1();
			double recall[] = matrix.getRecall();
			double precision[] = matrix.getPrecision();
			for(int i = 0; i < 3; i++){
				this.meanRecall[i] += recall[i];
				this.meanPrecision[i] += precision[i];
			}
		}
		meanAcc /= 10;
		meanMacroF1 /= 10;
		for (int i = 0; i < 3; i++) {
			meanRecall[i] /= 10;
			meanPrecision[i] /= 10;
		}
	}
	
	public void printResults(){
		calculeMeans();
		int index = 1;
		for(ConfusionMatrix matrix : this.confusionMatrixs){
			System.out.println("========== CROSS VALIDATION "+index+" ==========");
			System.out.println(matrix);
			index++;
		}
		System.out.println("========== MEAN RESULTS ==========");
		int classification;
		for(int j=0; j<3; j++) {
			if(j==0) classification = -1;
			else if(j==1) classification = 0;
			else classification = 1;
			System.out.println("Mean Precision for class " + classification + ": " + this.meanPrecision[j]);
			System.out.println("Mean Recall for class " + classification + ": " + this.meanRecall[j]);
		}
		System.out.println("Mean Accuracy: " + meanAcc);
		System.out.println("Mean MacroF1: " + meanMacroF1); 
	}

}
