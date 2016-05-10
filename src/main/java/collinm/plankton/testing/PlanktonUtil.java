package collinm.plankton.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import collinm.framework.data.TempDeserCollection;
import collinm.framework.json.RecordModule;

public class PlanktonUtil {

	private static Logger logger = LoggerFactory.getLogger(PlanktonUtil.class);

	/**
	 * 
	 * @param filePath
	 *            path to input JSON file
	 * @param sc
	 *            active <code>JavaSparkContext</code>
	 * @param k
	 *            number of splits
	 * @return data from JSON file loaded into <code>k</code> splits
	 */
	public static List<JavaRDD<LabeledPoint>> readData(Path filePath, JavaSparkContext sc, int k) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new RecordModule());

		// Read data from JSON file
		logger.info("Reading training data from " + filePath.toString());
		TempDeserCollection temp = null;
		try (BufferedReader reader = Files.newBufferedReader(filePath)) {
			temp = mapper.readValue(reader, TempDeserCollection.class);
		} catch (IOException io) {
			logger.error("Could not read input file!", io);
		}

		// Perform stratified sampling
		logger.info("Performing " + k + "-fold sampling");
		List<ArrayList<LabeledPoint>> samples = new ArrayList<>(k);
		for (int i = 0; i < k; i++)
			samples.add(new ArrayList<LabeledPoint>());
		for (int i = 0; i < temp.points.size(); i++)
			samples.get(i % k).add(temp.points.get(i));

		return samples.stream().map(l -> sc.parallelize(l)).collect(Collectors.toList());
	}

	/**
	 * 
	 * @param filePath
	 *            path to input JSON file
	 * @param sc
	 *            active <code>JavaSparkContext</code>
	 * @return data from JSON file loaded into one RDD
	 */
	public static JavaRDD<LabeledPoint> readData(Path filePath, JavaSparkContext sc) {
		return readData(filePath, sc, 1).get(1);
	}
	
	public static void writeMetrics(Path outputDir, List<ConfusionMatrix> matrices) {
		try {
			Files.createDirectories(outputDir);
		} catch (IOException io) {
			logger.error("Could not create output directory!", io);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputDir.toString(), "metrics.csv"))) {
			writer.write(ConfusionMatrix.toMetricsCSV(matrices));
		} catch (IOException io) {
			logger.error("Could not wite metrics out to file!", io);
		}
	}
	
	public static void writeMatrices(Path outputDir, List<ConfusionMatrix> matrices) {
		try {
			Files.createDirectories(outputDir);
		} catch (IOException io) {
			logger.error("Could not create output directory!", io);
		}
		
		for (int i = 0; i < matrices.size(); i++) {
			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputDir.toString(), "split" + i + ".csv"))) {
				writer.write(matrices.get(i).toCSV());
			} catch (IOException io) {
				logger.error("Could not wite confusion matrix out to file!", io);
			}
		}
	}
}
