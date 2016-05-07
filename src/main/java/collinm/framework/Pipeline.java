package collinm.framework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collinm.framework.data.Record;

public class Pipeline {

	private static Logger logger = LoggerFactory.getLogger(Pipeline.class);
	
	private final int batchSize;
	private DataSource source;
	private List<Processor> steps;
	private List<DataSink> sinks;

	public Pipeline() {
		this(1000);
	}

	public Pipeline(int batchSize) {
		this.batchSize = batchSize;
		this.steps = new ArrayList<>();
		this.sinks = new ArrayList<>();
	}

	public void setDataSource(DataSource src) {
		this.source = src;
	}

	public void addProcessor(Processor p) {
		this.steps.add(p);
	}

	public void addDataSink(DataSink sink) {
		this.sinks.add(sink);
	}

	public void run() {
		// Iterate until we run out of data
		logger.info("Starting pipeline");
		int batchNum = 0;
		while (this.source.hasNext()) {
			// Read input
			logger.info("Started reading batch [" + batchNum + "]");
			List<Record> batch = new ArrayList<>(this.batchSize);
			for (int i = 0; i < this.batchSize && source.hasNext(); i++)
				batch.add(source.next());
			logger.info("Finished reading batch [" + batchNum + "]");

			// Process records
			logger.info("Started processing batch [" + batchNum + "]");
			Stream<Record> data = batch.parallelStream();
			for (Processor proc : this.steps)
				data = data.map(proc::map_process);
			batch = data.collect(Collectors.toList());
			logger.info("Finished processing batch [" + batchNum + "]");

			// Write output
			logger.info("Started writing out batch [" + batchNum + "]");
			for (DataSink sink : this.sinks) {
				try {
					for (Record r : batch)
						sink.writeRecord(r);
				} catch (IOException io) {
					throw new RuntimeException(io);
				}
			}
			logger.info("Finished writing out batch [" + batchNum + "]");
			
			batchNum++;
		}
		
		logger.info("All batches complete");
	}

	public void close() {
		// Close data sinks
		for (DataSink sink : this.sinks) {
			try {
				sink.close();
			} catch (IOException io) {
				logger.error("Problem closing data sink!", io);
			}
		}
		
		// Close processors
		this.steps.forEach(s -> s.close());
	}
}
