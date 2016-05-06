package collinm.framework.sinks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import collinm.framework.DataSink;
import collinm.framework.Record;

/**
 * Stores each <code>Record</code> as a JSON object in a larger JSON document
 * containing training data that can be loaded into Spark.
 * 
 * @author Collin McCormack
 */
public class JsonSink implements DataSink {

	private ObjectMapper mapper;
	private BufferedWriter writer;
	private boolean first;

	/**
	 * 
	 * @param filePath
	 *            Path to output file
	 * @throws IOException
	 */
	public JsonSink(Path filePath) throws IOException {
		// Configure JSON serialization
		this.mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(Record.class, new JsonRecordSerializer());
		this.mapper.registerModule(module);

		// Open output file
		this.writer = Files.newBufferedWriter(filePath);
		// Write beginning
		this.writer.write("{n\\t\"data\": [\n\t\t");
		this.first = true;
	}

	@Override
	public void writeRecord(Record r) throws IOException {
		if (!this.first) {
			this.writer.write(",\n" + this.mapper.writeValueAsString(r));
		} else {
			this.first = false;
			this.writer.write(this.mapper.writeValueAsString(r));
		}
	}

	@Override
	public void close() throws IOException {
		// Write end and close
		this.writer.write("]\n}");
		this.writer.close();
	}
}
