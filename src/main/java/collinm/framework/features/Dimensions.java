package collinm.framework.features;

import org.opencv.core.Mat;

import collinm.framework.Processor;
import collinm.framework.data.Record;
import collinm.framework.data.Vector;

/**
 * Place a new <code>Vector</code> of length 2 at
 * <code>"feature-dimensions"</code> containing the height and width of the
 * image.
 * 
 * @author Collin McCormack
 */
public class Dimensions extends Processor {

	public static final String KEY = "feature-dimensions";

	public Dimensions() {
	}

	@Override
	public void process(Record r) {
		Mat img = (Mat) r.get("img");
		Vector v = new Vector(2);
		v.set(0, img.height());
		v.set(1, img.width());
		r.set(KEY, v);
	}

}
