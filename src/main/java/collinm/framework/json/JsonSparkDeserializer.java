package collinm.framework.json;

import java.io.IOException;
import java.util.Iterator;

import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import collinm.framework.data.LabeledPointWithId;
import collinm.framework.data.TempDeserCollection;

public class JsonSparkDeserializer extends JsonDeserializer<TempDeserCollection> {

	@Override
	public TempDeserCollection deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode root = jp.getCodec().readTree(jp);

		Iterator<JsonNode> instanceIter = root.get("data").elements();
		TempDeserCollection points = new TempDeserCollection();
		while (instanceIter.hasNext())
			points.add(deserializeInstance(instanceIter.next()));
		
		return points;
	}
	
	private LabeledPoint deserializeInstance(JsonNode node) {
		String id = node.get("ID").asText();
		int label = node.get("label").asInt();
		int featSize = node.get("features-size").asInt();
		
		double[] features = new double[featSize];
		Iterator<JsonNode> vecIter = node.get("features").elements();
		for (int i = 0; vecIter.hasNext(); i++)
			features[i] = vecIter.next().asDouble();

		return new LabeledPointWithId(id, label, Vectors.dense(features));
	}

}
