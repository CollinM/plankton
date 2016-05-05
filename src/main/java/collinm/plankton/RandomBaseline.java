package collinm.plankton;

import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import collinm.framework.Endpoint;
import collinm.framework.Pipeline;
import collinm.framework.Record;
import collinm.framework.endpoints.AttributesRecorder;
import collinm.framework.predict.RandomPredictor;

public class RandomBaseline {
	
	public static void main(String[] args) {
		// Configuration
		SparkConf conf = new SparkConf().setMaster("local[4]").setAppName("Random-Baseline")
				.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
		JavaSparkContext jsc = new JavaSparkContext(conf);
		//SQLContext sqlContext = new SQLContext(jsc);
		
		// Setup pipeline
		System.out.println("Creating pipeline...");
		Pipeline pipeline = new Pipeline();
		pipeline.add(new RandomPredictor()
				.setLabelKey("predict-label")
				.setLabels("src/main/resources/all_classes.txt"));
		System.out.println("Done");
		
		// Load dataset
		System.out.println("Loading dataset...");
		JavaRDD<Record> plankton = jsc.parallelize(PlanktonUtils.loadDataset("data/train"));
		System.out.println("Done");
		
		// Process dataset
		System.out.println("Processing dataset...");
		plankton = pipeline.run(plankton);
		System.out.println("Done");
		
		// Write out dataset
		System.out.println("Writing output...");
		Endpoint ep = new AttributesRecorder().setKeys(Arrays.asList("gold-label", "predict-label")).setOutputPath("predictions.txt");
		ep.record(plankton);
		System.out.println("Done");
		
		jsc.close();
	}

}
