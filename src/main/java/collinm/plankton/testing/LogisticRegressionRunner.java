package collinm.plankton.testing;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collinm.framework.data.LabeledPointWithId;

public class LogisticRegressionRunner {

	private static Logger logger = LoggerFactory.getLogger(LogisticRegressionRunner.class);

	public static void main(String[] args) {
		// Setup Spark
		SparkConf conf = new SparkConf().setAppName("LogisticRegression").setMaster("local[7]");
		JavaSparkContext sc = new JavaSparkContext(conf);
		int k = 5;

		List<ConfusionMatrix> metrics = new ArrayList<>(k);

		// Read data
		logger.info("Reading in data");
		List<JavaRDD<LabeledPoint>> samples = PlanktonUtil.readData(Paths.get("output/experiment1.json"), sc, k);

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
			train.cache();

			logger.info("Training model");
			LogisticRegressionModel model = new LogisticRegressionWithLBFGS().setNumClasses(121).run(train.rdd());
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
		
		PlanktonUtil.writeMetrics(Paths.get("doc/results/experiment1.csv"), metrics);

		double precision = metrics.stream().mapToDouble(c -> c.precision() / k).reduce(Double::sum).getAsDouble();
		double recall = metrics.stream().mapToDouble(c -> c.recall() / k).reduce(Double::sum).getAsDouble();
		double f1 = metrics.stream().mapToDouble(c -> c.f1() / k).reduce(Double::sum).getAsDouble();
		logger.info("Average Precision = " + precision);
		logger.info("Average Recall = " + recall);
		logger.info("Average F1 = " + f1);

		sc.close();
	}

}
