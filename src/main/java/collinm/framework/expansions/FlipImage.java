package collinm.framework.expansions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import collinm.framework.Expander;
import collinm.framework.data.Record;

/**
 * Flip the image (held in <code>"img"</code> horizontally or vertically.
 * 
 * @author Collin McCormack
 */
public class FlipImage extends Expander {

	private final String suffix;
	private final int flipCode;

	/**
	 * 
	 * @param horizontal
	 *            <code>true</code> to flip the image horizontally,
	 *            <code>false</code> for vertically
	 */
	public FlipImage(boolean horizontal) {
		if (horizontal) {
			this.suffix = "_fliph";
			this.flipCode = 1;
		} else {
			this.suffix = "_flipv";
			this.flipCode = 0;
		}
	}

	@Override
	public Stream<Record> expand(Record r) {
		List<Record> expansions = new ArrayList<>();
		expansions.add(r);

		// Flip image
		Record copy = r.copy(this.suffix);
		Mat img = (Mat) copy.get("img");
		Mat flipped = new Mat(img.height(), img.width(), CvType.CV_64FC3);
		Core.flip(img, flipped, this.flipCode);
		copy.set("img", flipped);
		expansions.add(copy);

		return expansions.parallelStream();
	}

}
