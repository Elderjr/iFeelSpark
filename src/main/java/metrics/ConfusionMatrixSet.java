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
			if(!Double.isNaN(matrix.getAccuracy())){
				this.meanAcc += matrix.getAccuracy();
			}
			if(!Double.isNaN(matrix.getMacroF1())){
				this.meanMacroF1 += matrix.getMacroF1();
			}
			double recall[] = matrix.getRecall();
			double precision[] = matrix.getPrecision();
			for(int i = 0; i < 3; i++){
				if(!Double.isNaN(recall[i])){
					this.meanRecall[i] += recall[i];
				}
				if(!Double.isNaN(precision[i])){
					this.meanPrecision[i] += precision[i];
				}
			}
		}
		meanAcc /= 10;
		meanMacroF1 /= 10;
		for (int i = 0; i < 3; i++) {
			meanRecall[i] /= 10;
			meanPrecision[i] /= 10;
		}
	}
	
	public void printCrossValidationsMatrix(){
		int index = 1;
		for(ConfusionMatrix matrix : this.confusionMatrixs){
			System.out.println("========== CROSS VALIDATION "+index+" ==========");
			System.out.println(matrix);
			index++;
		}
	}
	public void printResultsLatex(String methodName){
		calculeMeans();
		int neg = 0, neu = 1, pos = 2;
		System.out.println("\\multirow{3}{*}{" +methodName+"} & Pos: "+String.format("%.2f", this.meanPrecision[pos] * 100.0)+"\\% & Pos: "+String.format("%.2f", this.meanRecall[pos] * 100.0)+"\\% "+
				"& \\multirow{3}{*}{"+String.format("%.2f", this.meanAcc * 100.0)+"\\%} & \\multirow{3}{*}{"+String.format("%.2f", this.meanMacroF1 * 100.0)+"\\%} \\\\"+
				"& Neu: "+String.format("%.2f", this.meanPrecision[neu] * 100.0)+"\\% & Neu: "+String.format("%.2f", this.meanRecall[neu] * 100.0)+"\\% & & \\\\"+
				"& Neg: "+String.format("%.2f", this.meanPrecision[neg] * 100.0)+"\\% & Neg: "+String.format("%.2f", this.meanRecall[neg] * 100.0)+"\\% & & \\\\ \\hline");
	}
	
	public void printResults(){
		calculeMeans();
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
