package methods;

public class MethodTest extends Method{

	@Override
	public void loadDictionaries() {
		
	}

	@Override
	public int analyseText(String text) {
		if(text.length() > 100){
			return Method.POSITIVE;
		}else{
			return Method.NEGATIVE;
		}
	}

}
