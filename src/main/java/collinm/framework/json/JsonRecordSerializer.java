package collinm.framework.json;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import collinm.framework.data.Record;
import collinm.framework.data.Vector;
import collinm.framework.io.PlanktonSource;

public class JsonRecordSerializer extends JsonSerializer<Record> {

	public void serialize(Record record, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.writeStartObject();

		// Metadata
		jgen.writeStringField("ID", record.id());
		jgen.writeNumberField("label", (Integer) record.get(PlanktonSource.GOLD_LABEL_KEY));

		// Numerical features
		List<String> keys = record.getKeys();
		keys = keys.stream().sorted().filter(s -> s.startsWith("feature")).collect(Collectors.toList());
		jgen.writeArrayFieldStart("features");
		int size = 0;
		for (String k : keys) {
			Iterator<Double> iter = ((Vector) record.get(k)).getIterator();
			while (iter.hasNext()) {
				jgen.writeNumber(iter.next());
				size++;
			}
		}
		jgen.writeEndArray();
		// Write length of features array for convenience on deserialization
		jgen.writeNumberField("features-size", size);

		// Finish
		jgen.writeEndObject();
	}

}
