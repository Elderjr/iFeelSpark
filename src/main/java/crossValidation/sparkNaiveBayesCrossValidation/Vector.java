package crossValidation.sparkNaiveBayesCrossValidation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Vector {
	private int classification;
	private HashMap<Integer, Integer> dimensions;
	
	public Vector(int classification) {
		this.setClassification(classification);
		this.setDimensions(new HashMap<Integer, Integer>());
	}

	public int getClassification() {
		return classification;
	}

	public void setClassification(int classification) {
		this.classification = classification;
	}

	HashMap<Integer, Integer> getDimensions() {
		return dimensions;
	}

	void setDimensions(HashMap<Integer, Integer> dimensions) {
		this.dimensions = dimensions;
	}
	
	public void addDimension(int index) {
		this.dimensions.put(index, 0);
	}
	
	public void incrementDimension(int index) {
		if(this.dimensions.containsKey(index)) {
			int value = this.dimensions.get(index);
			this.dimensions.put(index, value+1);
		}else {
			this.dimensions.put(index, 1);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.classification);       
		Map<Integer, Integer> treeMap = new TreeMap<Integer, Integer>(this.dimensions);
		for (Entry<Integer, Integer> entry : treeMap.entrySet()) {
		    builder.append(" ").append(entry.getKey()).append(":").append(entry.getValue());
		}
		return builder.toString();
	}
	
	public static Vector createVector(String tweet, HashMap<String, Integer> map, String classification) {
		int max = map.size();
		String[] words = tweet.split("\\s+");
		Vector vector = new Vector(Integer.parseInt(classification.trim()));
		vector.addDimension(max);
		for(String word: words) {
			vector.incrementDimension(map.get(word));
		}
		return vector;
	}
}
