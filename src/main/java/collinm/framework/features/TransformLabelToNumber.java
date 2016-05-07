package collinm.framework.features;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import collinm.framework.Processor;
import collinm.framework.data.Record;
import collinm.framework.io.PlanktonSource;

import org.slf4j.LoggerFactory;

/**
 * Transform the gold standard label to a number, specifically the index of the
 * label in a master list.
 * 
 * @author Collin McCormack
 */
public class TransformLabelToNumber extends Processor {

	private static Logger logger = LoggerFactory.getLogger(TransformLabelToNumber.class);
	
	private Map<String, Integer> labels;

	public TransformLabelToNumber(Path pathToLabelFile) {
		this.labels = new HashMap<>();
		try (BufferedReader reader = Files.newBufferedReader(pathToLabelFile)) {
			List<String> lines = reader.lines().map(s -> s.trim()).collect(Collectors.toList());
			for (int i = 0; i < lines.size(); i++)
				this.labels.put(lines.get(i), i);
		} catch (IOException io) {
			logger.error("Problem opening the labels file!", io);
		}
	}

	@Override
	public void process(Record r) {
		r.set(PlanktonSource.GOLD_LABEL_KEY, this.labels.get(r.get(PlanktonSource.GOLD_LABEL_KEY)));
	}
}
