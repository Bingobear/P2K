package master.keyEx;



import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;


public class LangDetect {

	public String detect(String parsedText, boolean first)
			throws LangDetectException {
		// TODO Auto-generated method stub
		String text = "";
		if(parsedText.length()<150){
			text = parsedText;
		}else{
			 text = parsedText.substring(150);
		}
		
		if (first) {
			try {
				DetectorFactory.loadProfile("res/profiles");
			} catch (LangDetectException e) {
				// TODO Auto-generated catch block
				System.out.println(first);
				e.printStackTrace();
			}
		}
		Detector detector = DetectorFactory.create();
		detector.append(text);

		String lang = detector.detect();
		return lang;
	}

}
