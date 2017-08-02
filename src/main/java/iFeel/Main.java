package iFeel;

public class Main {

	public static void main(String[] args) {
		String phrase = "I'm sad";
		Method methods[] = { MethodCreator.getInstance().createMethod(Method.AFINN),
				MethodCreator.getInstance().createMethod(Method.EMOLEX),
				MethodCreator.getInstance().createMethod(Method.EMOTICONDS),
				MethodCreator.getInstance().createMethod(Method.EMOTICONS),
				MethodCreator.getInstance().createMethod(Method.HAPPINESS_INDEX),
				MethodCreator.getInstance().createMethod(Method.MPQA),
				MethodCreator.getInstance().createMethod(Method.OPINION),
				MethodCreator.getInstance().createMethod(Method.PANAST),
				MethodCreator.getInstance().createMethod(Method.SANN),
				MethodCreator.getInstance().createMethod(Method.SASA),
				MethodCreator.getInstance().createMethod(Method.SENTIC_NET),
				MethodCreator.getInstance().createMethod(Method.SENTIMENT_140),
				MethodCreator.getInstance().createMethod(Method.SENTI_STRENGTH),
				MethodCreator.getInstance().createMethod(Method.SENTI_WORD_NET),
				MethodCreator.getInstance().createMethod(Method.SO_CAL),
				MethodCreator.getInstance().createMethod(Method.STANFORD),
				MethodCreator.getInstance().createMethod(Method.UMIGON),
				MethodCreator.getInstance().createMethod(Method.VADER),
				MethodCreator.getInstance().createMethod(Method.NRC_HashTag) };
		System.out.println("Phrase: " + phrase);
		for (Method m : methods) {
			int result = m.analyseText(phrase);
			if (result == Method.POSITIVE) {
				System.out.println(m.getName() + ": POSITIVE");
			} else if (result == Method.NEGATIVE) {
				System.out.println(m.getName() + ": NEGATIVE");
			} else {
				System.out.println(m.getName() + ": NEUTRAL");
			}
		}
	}
}
