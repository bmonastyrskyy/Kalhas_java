package edu.ucdavis.gc.bm.crossvalidation;

import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import edu.ucdavis.gc.hmmAssignment.DescriptorAssignment;

public class TrainningWEKA {

	private StringBuilder output = new StringBuilder("");
	
	public TrainningWEKA(List<DescriptorAssignment> assigns) {
		this.process(assigns);
	}

	public String toString(){
		return this.output.toString();
	}
	
	private void process(List<DescriptorAssignment> assigns) {
		/*
		 * The code was addapted from the example at
		 * http://weka.wikispaces.com/Programmatic+Use
		 */
		// just one numeric attribute - hmmScore in our case
		Attribute attribute1 = new Attribute("firstNumeric");

		// class vector - quality status of the assignment
		FastVector fvClassVal = new FastVector(3);
		fvClassVal.addElement("1"); // good assignments
		fvClassVal.addElement("2"); // neither god nor bad
		fvClassVal.addElement("3"); // really bad assignments
		Attribute classAttribute = new Attribute("theClass", fvClassVal);

		// feature vector
		FastVector fvWekaAttributes = new FastVector(2);
		fvWekaAttributes.addElement(attribute1);
		fvWekaAttributes.addElement(classAttribute);

		// Create an empty training set
		int size = assigns.size();
		Instances trainingSet = new Instances("Rel", fvWekaAttributes, size);
		// Set class index
		trainingSet.setClassIndex(1);

		// fill up the training set with instances
		for (DescriptorAssignment ass : assigns) {
			Instance example = new Instance(2);
			example.setValue((Attribute) fvWekaAttributes.elementAt(0), ass
					.getHMMScores().get(0));
			example.setValue((Attribute) fvWekaAttributes.elementAt(1), ass
					.getQualityStatus().toString());
			// add the instance
			trainingSet.add(example);
		}

		// Create a naïve bayes classifier Classifier
		Classifier cModel = (Classifier) new NaiveBayes();
		try {
			cModel.buildClassifier(trainingSet);
			Evaluation eTest = new Evaluation(trainingSet);
			eTest.evaluateModel(cModel, trainingSet);
			// Print the result à la Weka explorer:
			String strSummary = eTest.toSummaryString();
			output.append(strSummary + "\n");
			double[][] cmMatrix = eTest.confusionMatrix();
			output.append("ConfusionMatrix\n");
			for(int i = 0 ; i < cmMatrix.length; i++){
				for(int j = 0; j < cmMatrix.length; j++){
					output.append(cmMatrix[i][j] + " ");
				}
				output.append("\n");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
