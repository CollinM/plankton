package collinm.plankton;

import java.nio.file.Paths;

import org.opencv.core.Core;

import collinm.framework.Pipeline;
import collinm.framework.sources.PlanktonSource;

// TODO make this into a general pipeline with data runner?
public class Experiment1 {

	public static void main(String[] args) {
		// Load OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// TODO Create pipeline
		Pipeline pipe = new Pipeline();
		pipe.setDataSource(new PlanktonSource(Paths.get("data/train")));
		
		// Run pipeline
		pipe.run();
		pipe.close();
	}

}
