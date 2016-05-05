package collinm.plankton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import collinm.framework.Record;

public final class PlanktonUtils {

	public static List<Record> loadDataset(String path) {
		List<Record> images = Collections.synchronizedList(new ArrayList<>());

		try (Stream<Path> dirs = Files.list(Paths.get(path))) {
			// for each folder, store gold label name
			dirs.forEach(d -> {
				String label = d.getFileName().toString();
				System.out.println("Processing " + label);
				// In each folder, create plankton image with loaded image and label
				try (Stream<Path> files = Files.list(d)) {
					files.parallel().forEach(f -> {
						try {
							images.add(new Record(f.getFileName().toString())
								.set("img", ImageIO.read(f.toFile()))
								.set("gold-label", label));
						} catch (IOException e) {
							e.printStackTrace();
						}
						});
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		return images;
	}
}
