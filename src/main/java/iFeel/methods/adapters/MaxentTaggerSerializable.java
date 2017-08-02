package iFeel.methods.adapters;

import java.io.Serializable;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class MaxentTaggerSerializable extends MaxentTagger implements Serializable{

	public MaxentTaggerSerializable(String modelFilePath){
		super(modelFilePath);
	}
}
