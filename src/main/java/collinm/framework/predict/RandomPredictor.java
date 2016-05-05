package collinm.framework.predict;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import collinm.framework.Processor;
import collinm.framework.Record;

/**
 * Assign a random label to the record under the specified key.
 * 
 * @author Collin McCormack
 */
public class RandomPredictor implements Processor {

	private static final long serialVersionUID = 1L;
	private String labelKey;
	private List<String> labels;
	private Random random;

	public RandomPredictor() {
		this.labelKey = "predicted-label";
		this.random = new Random();
		this.labels = new ArrayList<>();
	}

	@Override
	public Record process(Record r) {
		r.set(this.labelKey, this.labels.get(this.random.nextInt(this.labels.size())));
		return r;
	}

	public RandomPredictor setLabelKey(String labelKey) {
		this.labelKey = labelKey;
		return this;
	}

	public RandomPredictor setLabels(String filePath) {
		try {
			this.labels = Files.readAllLines(Paths.get(filePath))
					.stream()
					.map(s -> s.trim())
					.collect(Collectors.toCollection(ArrayList::new));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
}
