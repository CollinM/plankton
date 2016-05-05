package collinm.play;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class SparkTest {

	public static void main(String[] args) {
		// Configuration
		SparkConf conf = new SparkConf().setAppName("SparkTest").setMaster("local[2]");
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// Create data and load into RDD
		List<Integer> nums = new ArrayList<Integer>(100000);
		for (int i = 0; i < 100000; i++)
			nums.add(i);
		JavaRDD<Integer> distNums = sc.parallelize(nums, 8);
		
		int sum = distNums.reduce((a, b) -> a + b);
		System.out.println(sum);
	}
}
