package collinm.plankton.testing;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Multilayer Perception Runner.
 * 
 * @author Collin McCormack
 */
public class MlpRunner {

	private static Logger logger = LoggerFactory.getLogger(MlpRunner.class);

	public static void main(String[] args) {
		Path inputFile = Paths.get(args[0]);
		Path outputDir = Paths.get(args[1]);
		int numFeatures = Integer.parseInt(args[2]);
		int k = Integer.parseInt(args[3]);
		
		// Setup Spark
		SparkConf conf = new SparkConf().setAppName("MultilayerPerceptron");
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sql = new SQLContext(sc);

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
			// Convert to DataFrame
			DataFrame trainDF = sql.createDataFrame(train, LabeledPoint.class);
			DataFrame testDF = sql.createDataFrame(test, LabeledPoint.class);
			trainDF.cache();
			
			// Set parameters
			int layers[] = new int[] {numFeatures, 500, 250, 121};
			
			logger.info("Training model");
			MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier()
					.setLayers(layers)
					.setBlockSize(256)
					.setSeed(42)
					.setMaxIter(1000);
			MultilayerPerceptronClassificationModel model = trainer.fit(trainDF);
			logger.info("Done");

			trainDF.unpersist();

			logger.info("Evaluating performance");
			DataFrame results = model.transform(testDF);
			DataFrame evalPairs = results.select("prediction", "label");
			
			List<Triplet<String, Double, Double>> idLabelPredictions = evalPairs.toJavaRDD().map((Row row) -> {
				Double prediction = (Double) row.get(0);
				Double actual = (Double) row.get(1);
				return Triplet.with("", actual, prediction);
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
