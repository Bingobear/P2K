package test.tester;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public class LangDetect {

	public void detect(String parsedText) throws LangDetectException {
		// TODO Auto-generated method stub
		try {
			DetectorFactory.loadProfile("res/profiles");
		} catch (LangDetectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Detector detector = DetectorFactory.create();
		detector.append(parsedText);
	}

}
