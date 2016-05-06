package collinm.framework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Pipeline {

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
		while (this.source.hasNext()) {
			// Read input
			List<Record> batch = new ArrayList<>(this.batchSize);
			for (int i = 0; i < this.batchSize && source.hasNext(); i++)
				batch.add(source.next());

			// Process records
			Stream<Record> data = batch.parallelStream();
			for (Processor proc : this.steps)
				data = data.map(proc::map_process);

			// Write output
			batch = data.collect(Collectors.toList());
			for (DataSink sink : this.sinks) {
				try {
					for (Record r : batch)
						sink.writeRecord(r);
				} catch (IOException io) {
					throw new RuntimeException(io);
				}
			}
				
		}
	}

	public void close() {
		for (DataSink sink : this.sinks) {
			try {
				sink.close();
			} catch (IOException io) {
				io.printStackTrace();
			}
		}
	}
}
