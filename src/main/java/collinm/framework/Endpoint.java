package collinm.framework;

import java.io.Serializable;

import org.apache.spark.api.java.JavaRDD;

public interface Endpoint extends Serializable {

	public void record(JavaRDD<Record> records);
}
