package collinm.framework.features;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import collinm.framework.Processor;
import collinm.framework.data.Record;
import collinm.framework.io.PlanktonSource;

/**
 * Scale the image of the <code>Record</code> to be square with each dimension
 * equal to the target dimension.
 * <p>
 * Take the largest dimension of the image and scale it to the target dimension,
 * scaling the lesser dimension at the same rate. Then, pad the shorter
 * dimension with white pixels until it equals the target dimension. This
 * approach doesn't fill the image entirely, but it minimizes the effect of
 * interpolation artifacts.
 * </p>
 * 
 * @author Collin McCormack
 */
public class NormalizeImageSize extends Processor {
	
	private double targetDimension;
	
	public NormalizeImageSize(double targetDimension) {
		this.targetDimension = targetDimension;
	}
	
	@Override
	public void process(Record r) {
		Mat img = (Mat) r.get(PlanktonSource.IMAGE_KEY);
		int h = img.height();
		int w = img.width();

		// Resize image based on largest edge, do NOT change the image ratio!
		// Pad shorter dimension of image to match the desired edge length
		if (h > w) {
			// Resize to get height to desired size
			double scale = this.targetDimension / h;
			Imgproc.resize(img, img, new Size(scale * w, this.targetDimension), 0, 0, Imgproc.INTER_CUBIC);
			// Pad width to achieve a square image
			w = img.width();
			int diff = (int) (Math.round(this.targetDimension) - w);
			int left = diff / 2;
			int right = (int) (Math.round(this.targetDimension) - w - left);
			Core.copyMakeBorder(img, img, 0, 0, left, right, Core.BORDER_CONSTANT, new Scalar(255));
		} else {
			// Resize to get width to desired size
			double scale = this.targetDimension / w;
			Imgproc.resize(img, img, new Size(this.targetDimension, scale * h), 0, 0, Imgproc.INTER_CUBIC);
			// Pad height to achieve a square image
			h = img.height();
			int diff = (int) (Math.round(this.targetDimension) - h);
			int top = diff / 2;
			int bottom = (int) (Math.round(this.targetDimension) - h - top);
			Core.copyMakeBorder(img, img, top, bottom, 0, 0, Core.BORDER_CONSTANT, new Scalar(255, 255, 255));
		}
		
		r.set(PlanktonSource.IMAGE_KEY, img);
	}

	
}
