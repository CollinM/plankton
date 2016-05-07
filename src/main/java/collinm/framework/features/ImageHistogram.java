package collinm.framework.features;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import collinm.framework.Processor;
import collinm.framework.data.Record;
import collinm.framework.data.Vector;

/**
 * Place a new <code>Vector</code> of length 255 at
 * <code>"feature-full-histogram"</code> containing a count of the pixels with
 * each indexed value.
 * 
 * @author Collin McCormack
 */
public class ImageHistogram extends Processor {

	public static final String KEY = "feature-full-histogram";

	public ImageHistogram() {
	}

	@Override
	public void process(Record r) {
		Vector v = new Vector(256);
		Mat img = (Mat) r.get("img");

		// This is somewhat bulky and ugly, but it's 30% faster than requesting
		// one pixel at a time.
		img.convertTo(img, CvType.CV_64FC3);
		double[] pixels = new double[(int) img.total() * img.channels()];
		img.get(0, 0, pixels);
		for (int i = 0; i < pixels.length; i += 3)
			v.increment((int) pixels[i]);

		r.set(KEY, v);
	}
}
