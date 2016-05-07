package collinm.plankton.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import collinm.framework.data.TempDeserCollection;
import collinm.framework.json.RecordModule;
import scala.Tuple2;

public class LogisticRegressionRunner {

	private static Logger logger = LoggerFactory.getLogger(LogisticRegressionRunner.class);

	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setAppName("LogisticRegression").setMaster("local[4]");
		JavaSparkContext sc = new JavaSparkContext(conf);
		//SQLContext sqlContext = new SQLContext(sc);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new RecordModule());

		// TODO make this file dynamic
		// TODO make this into a function...
		logger.info("Reading training data");
		TempDeserCollection temp = null;
		try (BufferedReader reader = Files.newBufferedReader(Paths.get("output/experiment1.json"))) {
			temp = mapper.readValue(reader, TempDeserCollection.class);
		} catch (IOException io) {
			logger.error("Could not read input file!", io);
		}
		//DataFrame training = sqlContext.createDataFrame(temp.points, LabeledPointWithId.class);
		JavaRDD<LabeledPoint> rdd = sc.parallelize(temp.points);
		
		JavaRDD<LabeledPoint>[] splits = rdd.randomSplit(new double[] {0.8, 0.2});
		JavaRDD<LabeledPoint> train = splits[0].cache();
		JavaRDD<LabeledPoint> test = splits[1];
		logger.info("Done");
		
		logger.info("Training model");
		LogisticRegressionModel model = new LogisticRegressionWithLBFGS()
				.setNumClasses(121)
				.run(train.rdd());
		logger.info("Done");
		
		logger.info("Evaluating performance");
		JavaRDD<Tuple2<Object, Object>> predictionAndLabels = test.map(
			      new Function<LabeledPoint, Tuple2<Object, Object>>() {
			        public Tuple2<Object, Object> call(LabeledPoint p) {
			          Double prediction = model.predict(p.features());
			          return new Tuple2<Object, Object>(prediction, p.label());
			        }
			      }
			    );
		MulticlassMetrics metrics = new MulticlassMetrics(predictionAndLabels.rdd());
	    double precision = metrics.precision();
	    double recall = metrics.recall();
	    double f1 = metrics.fMeasure();
	    logger.info("Precision = " + precision);
	    logger.info("Recall = " + recall);
	    logger.info("F1 = " + f1);
	    
	    sc.close();
	}

}
