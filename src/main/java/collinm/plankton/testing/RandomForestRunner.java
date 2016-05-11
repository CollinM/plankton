package collinm.plankton.testing;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.RandomForest;
import org.apache.spark.mllib.tree.model.RandomForestModel;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collinm.framework.data.LabeledPointWithId;

public class RandomForestRunner {
	
	private static Logger logger = LoggerFactory.getLogger(RandomForestRunner.class);

	public static void main(String[] args) {
		Path inputFile = Paths.get(args[0]);
		Path outputDir = Paths.get(args[1]);
		int trees = Integer.parseInt(args[2]);
		int depth = Integer.parseInt(args[3]);
		int k = Integer.parseInt(args[4]);
		
		// Setup Spark
		SparkConf conf = new SparkConf().setAppName("RandomForest");
		JavaSparkContext sc = new JavaSparkContext(conf);

		// Setup metrics container
		List<ConfusionMatrix> metrics = new ArrayList<>(k);

		// Read data
		logger.info("Reading in data");
		List<JavaRDD<LabeledPoint>> samples = PlanktonUtil.readData(inputFile, sc, k);
		
		for (int split = 0; split < k; split++) {
			logger.info("Starting batch [" + split + "]");

			// Collect training data into single RDD
			JavaRDD<LabeledPoint> train = sc.emptyRDD();
			JavaRDD<LabeledPoint> test = null;
			for (int i = 0; i < k; i++) {
				if (i != split)
					train = train.union(samples.get(i));
				else
					test = samples.get(i);
			}
			train.cache();

			// Set parameters
			int numClasses= 121;
			HashMap<Integer, Integer> categoricalFeaturesInfo = new HashMap<Integer, Integer>();
			Integer numTrees = trees;
			String featureSubsetStrategy = "auto";
			String impurity = "gini";
			Integer maxDepth = depth;
			Integer maxBins = 128;
			Integer seed = 42;
			
			logger.info("Training model");
			RandomForestModel model = RandomForest.trainClassifier(train, numClasses,
					categoricalFeaturesInfo, numTrees, featureSubsetStrategy, impurity,
					maxDepth, maxBins, seed);
			logger.info("Done");

			train.unpersist();

			logger.info("Evaluating performance");
			List<Triplet<String, Double, Double>> idLabelPredictions = test.map(
					p -> {
						Double prediction = model.predict(p.features());
						String id = ((LabeledPointWithId) p).getId();
						return Triplet.with(id, p.label(), prediction);
					}).collect();
			
			ConfusionMatrix cm = new ConfusionMatrix(121);
			metrics.add(cm);
			cm.measure(idLabelPredictions);
			
			logger.info("Batch [" + split + "]: Precision = " + cm.precision());
			logger.info("Batch [" + split + "]: Recall = " + cm.recall());
			logger.info("Batch [" + split + "]: F1 = " + cm.f1());
		}
		
		PlanktonUtil.writeMetrics(outputDir, metrics);
		PlanktonUtil.writeMatrices(outputDir, metrics);

		double precision = metrics.stream().mapToDouble(c -> c.precision() / k).reduce(Double::sum).getAsDouble();
		double recall = metrics.stream().mapToDouble(c -> c.recall() / k).reduce(Double::sum).getAsDouble();
		double f1 = metrics.stream().mapToDouble(c -> c.f1() / k).reduce(Double::sum).getAsDouble();
		logger.info("Average Precision = " + precision);
		logger.info("Average Recall = " + recall);
		logger.info("Average F1 = " + f1);

		sc.close();
	}

}
