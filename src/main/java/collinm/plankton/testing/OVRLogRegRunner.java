package collinm.plankton.testing;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.OneVsRest;
import org.apache.spark.ml.classification.OneVsRestModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One vs rest Logistic Regression Runner.
 * 
 * @author Collin McCormack
 */
public class OVRLogRegRunner {
	
	private static Logger logger = LoggerFactory.getLogger(OVRLogRegRunner.class);

	public static void main(String[] args) {
		Path inputFile = Paths.get(args[0]);
		Path outputDir = Paths.get(args[1]);
		int k = Integer.parseInt(args[2]);
		
		// Setup Spark
		SparkConf conf = new SparkConf().setAppName("OVRLogisticRegression");
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sql = new SQLContext(sc);

		// Setup metrics container
		List<ConfusionMatrix> metrics = new ArrayList<>(k);

		// Read data
		logger.info("Reading in data");
		List<JavaRDD<LabeledPoint>> samples = PlanktonUtil.readData(inputFile, sc, k);

		for (int split = 0; split < k; split++) {
			logger.info("Starting batch [" + split + "]");

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

			logger.info("Training model");
			LogisticRegression lr = new LogisticRegression();
			OneVsRest ovr = new OneVsRest().setClassifier(lr);
			OneVsRestModel ovrModel = ovr.fit(trainDF);
			logger.info("Done");

			train.unpersist();

			logger.info("Evaluating performance");
			DataFrame results = ovrModel.transform(testDF);
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
			
			PlanktonUtil.writeConfusionMatrix(outputDir, cm, "split" + split + ".csv");
			// This will write and overwrite many times, but at least this way I
			// can see intermediate output instead of having to wait until all
			// of the folds are finished
			PlanktonUtil.writeMetrics(outputDir, metrics);
		}

		double precision = metrics.stream().mapToDouble(c -> c.precision() / k).reduce(Double::sum).getAsDouble();
		double recall = metrics.stream().mapToDouble(c -> c.recall() / k).reduce(Double::sum).getAsDouble();
		double f1 = metrics.stream().mapToDouble(c -> c.f1() / k).reduce(Double::sum).getAsDouble();
		logger.info("Average Precision = " + precision);
		logger.info("Average Recall = " + recall);
		logger.info("Average F1 = " + f1);

		sc.close();
	}

}
