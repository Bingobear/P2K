package master.keyEx;

import java.util.ArrayList;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;

public class LangDetect {

	public String detect(String parsedText) throws LangDetectException {
		// TODO Auto-generated method stub
		String text = parsedText.substring(150);
		try {
			DetectorFactory.loadProfile("res/profiles");
		} catch (LangDetectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Detector detector = DetectorFactory.create();
		detector.append(text);
		
		String lang = detector.detect();
		return lang;
	}

}
