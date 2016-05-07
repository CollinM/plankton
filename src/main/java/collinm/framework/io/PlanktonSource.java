package collinm.framework.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;

import org.javatuples.Pair;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collinm.framework.DataSource;
import collinm.framework.data.Record;

/**
 * Loads the plankton training data lazily.
 * 
 * @author Collin McCormack
 */
public class PlanktonSource implements DataSource {

	private static Logger logger = LoggerFactory.getLogger(PlanktonSource.class);
	
	public static final String IMAGE_KEY = "img";
	public static final String GOLD_LABEL_KEY = "gold-label";

	private final Deque<Pair<Path, String>> inputFiles;

	/**
	 * 
	 * @param dirPath
	 *            Path to the root of the plankton training data
	 */
	public PlanktonSource(Path dirPath) {
		this.inputFiles = new ArrayDeque<Pair<Path, String>>();

		try (Stream<Path> dirs = Files.list(dirPath)) {
			dirs.forEach(d -> {
				String label = d.getFileName().toString();
				// In each folder, store path to image and label
				try (Stream<Path> files = Files.list(d)) {
					files.forEach(f -> this.inputFiles.addLast(Pair.with(f, label)));
				} catch (IOException io) {
					logger.error("Problem occurred while listing direcotory contents: [" + d.toString() + "]", io);
				}
			});
		} catch (IOException io) {
			logger.error("Problem occurred while listing direcotory contents: [" + dirPath.toString() + "]", io);
		}
	}

	@Override
	public boolean hasNext() {
		return this.inputFiles.size() > 0;
	}

	@Override
	public Record next() {
		Pair<Path, String> item = this.inputFiles.poll();
		Mat img = Imgcodecs.imread(item.getValue0().toAbsolutePath().toString());
		return new Record(item.getValue0().getFileName().toString()).set(IMAGE_KEY, img).set(GOLD_LABEL_KEY,
				item.getValue1());
	}
}
