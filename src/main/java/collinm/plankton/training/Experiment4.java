package collinm.plankton.training;

import java.io.IOException;
import java.nio.file.Paths;

import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collinm.framework.Pipeline;
import collinm.framework.features.Dimensions;
import collinm.framework.features.ImageHistogram;
import collinm.framework.features.NormalizeImageSize;
import collinm.framework.features.PixelCount;
import collinm.framework.features.TransformLabelToNumber;
import collinm.framework.io.JsonSink;
import collinm.framework.io.PlanktonSource;

public class Experiment4 {
	
	public static void main(String[] args) throws IOException {
		// Load OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		Logger logger = LoggerFactory.getLogger(Experiment4.class);

		// Create pipeline
		logger.info("Setting up pipeline...");
		Pipeline pipe = new Pipeline();
		pipe.setDataSource(new PlanktonSource(Paths.get("data/train")));
		pipe.addProcessor(new TransformLabelToNumber(Paths.get("data/all-classes.txt")));
		pipe.addProcessor(new Dimensions());
		pipe.addProcessor(new PixelCount());
		pipe.addProcessor(new NormalizeImageSize(128));
		pipe.addProcessor(new ImageHistogram());
		pipe.addDataSink(new JsonSink(Paths.get("output/experiment4.json")));

		// Run pipeline
		logger.info("Running pipeline...");
		pipe.run();
		pipe.close();
		logger.info("Done");
	}

}
