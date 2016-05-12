package collinm.framework.features;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import collinm.framework.Processor;
import collinm.framework.data.Record;
import collinm.framework.data.Vector;

public class ImageHistogramBins extends Processor {

	public static final String KEY = "feature-histogram-bin";
	
	private int binSize;
	private int bins;
	private int lowCutoff;
	private int highCutoff;
	
	/**
	 * 
	 * @param binSize
	 *            size of each bin
	 * @param lowCutoff
	 *            inclusive low pixel value
	 * @param highCutoff
	 *            exclusive high pixel value
	 */
	public ImageHistogramBins(int binSize, int lowCutoff, int highCutoff) {
		this.binSize = binSize;
		this.lowCutoff = lowCutoff;
		this.highCutoff = highCutoff;
		this.bins =  (highCutoff - lowCutoff) / binSize + 1;
	}

	@Override
	public void process(Record r) {
		Vector v = new Vector(this.bins);
		Mat img = (Mat) r.get("img");

		// This is somewhat bulky and ugly, but it's 30% faster than requesting
		// one pixel at a time.
		img.convertTo(img, CvType.CV_64FC3);
		double[] pixels = new double[(int) img.total() * img.channels()];
		img.get(0, 0, pixels);
		int p = 0;
		for (int i = 0; i < pixels.length; i += 3) {
			p = (int) pixels[i];
			if (p >= this.lowCutoff && p < this.highCutoff)
				v.increment((p - this.lowCutoff) / this.binSize);
		}

		r.set(KEY, v);
	}

}
