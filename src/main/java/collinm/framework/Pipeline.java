package collinm.framework;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;

public class Pipeline {

	private List<Processor> steps;
	
	public Pipeline() {
		this.steps = new ArrayList<>();
	}
	
	public void add(Processor p) {
		this.steps.add(p);
	}
	
	public void insert(int i, Processor p) {
		this.steps.add(i, p);
	}
	
	public JavaRDD<Record> run(JavaRDD<Record> records) {
		JavaRDD<Record> data = records;
		for (Processor proc : this.steps)
			data = data.map(proc::process);
		return data;
	}
}
