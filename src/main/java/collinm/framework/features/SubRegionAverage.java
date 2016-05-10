package collinm.framework.features;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import collinm.framework.Processor;
import collinm.framework.data.Record;
import collinm.framework.data.Vector;

/**
 * Given a window size, construct a square window and make window-sized steps
 * over the rows and columns of the image. At each step, take the average value
 * of the pixels in the window and add the resulting value to a feature vector.
 * If the optional overlap is specified, each step will be
 * "window size - overlap" in magnitude, and thus each window measurement will
 * overlap by the specified amount of pixels.
 * 
 * @author Collin McCormack
 */
public class SubRegionAverage extends Processor {

	public static final String KEY = "feature-subregion-average";
	
	private int length;
	private int overlap;
	private int step;
	
	public SubRegionAverage(int length) {
		this(length, 0);
	}
	
	public SubRegionAverage(int length, int overlap) {
		this.length = length;
		this.overlap = overlap;
		this.step = length - overlap;
	}
	
	@Override
	public void process(Record r) {
		Mat img = (Mat) r.get("img");
		int h = img.height();
		int w = img.width();
		int c = img.channels();
		int rowLength = w * c;
		img.convertTo(img, CvType.CV_64FC3);
		double[] pixels = new double[(int) img.total() * c];
		img.get(0, 0, pixels);
		int div = ((h - this.length) / this.step) + 1;
		Vector v = new Vector(div * div);
		
		int index = 0;
		for (int rowStart = 0; rowStart < h - this.overlap; rowStart += this.step) {
			for (int colStart = 0; colStart < w - this.overlap; colStart += this.step) {
				int sum = 0;
				for (int row = 0; row < this.length; row++) {
					for (int col = 0; col < this.length; col++) {
						sum += pixels[rowStart * rowLength + row * rowLength + colStart * c + col * c];
					}
				}
				// Put average in vector 
				v.set(index++, sum / ((double) this.length * this.length));
			}
		}
		
		r.set(KEY, v);
	}
}
