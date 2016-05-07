package collinm.framework.features;

import org.opencv.core.Mat;

import collinm.framework.Processor;
import collinm.framework.data.Record;
import collinm.framework.data.Vector;

/**
 * Place a new <code>Vector</code> of length 1 at
 * <code>"feature-pixel-count"</code> containing the total number of pictures in
 * the image.
 * 
 * @author Collin McCormack
 */
public class PixelCount extends Processor {

	public static final String KEY = "feature-pixel-count";

	public PixelCount() {
	}

	@Override
	public void process(Record r) {
		Mat img = (Mat) r.get("img");
		Vector v = new Vector(1);
		v.set(0, img.total());
		r.set(KEY, v);

	}

}
