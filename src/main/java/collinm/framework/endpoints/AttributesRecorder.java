package collinm.framework.endpoints;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.apache.spark.api.java.JavaRDD;

import collinm.framework.Endpoint;
import collinm.framework.Record;

public class AttributesRecorder implements Endpoint {

	private static final long serialVersionUID = 1L;
	private List<String> attributeKeys;
	private String outputPath;
	
	public AttributesRecorder() {
		this.attributeKeys = new ArrayList<>();
		this.outputPath = "attributes.txt";
	}
	
	@Override
	public void record(JavaRDD<Record> records) {
		List<String> output = records.map(this::convert).collect();
		try {
			Files.write(Paths.get(this.outputPath), output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String convert(Record r) {
		StringJoiner joiner = new StringJoiner(",");
		joiner.add(r.id());
		this.attributeKeys.forEach(k -> joiner.add((String) r.get(k)));
		return joiner.toString();
	}
	
	public AttributesRecorder setKeys(List<String> keys) {
		this.attributeKeys = keys;
		return this;
	}
	
	public AttributesRecorder setOutputPath(String path) {
		this.outputPath = path;
		return this;
	}
}
