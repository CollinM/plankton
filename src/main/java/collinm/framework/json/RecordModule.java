package collinm.framework.json;

import com.fasterxml.jackson.databind.module.SimpleModule;

import collinm.framework.data.Record;
import collinm.framework.data.TempDeserCollection;

/**
 * Class of convenience for grouping for Jackson's de/serializers together for
 * use with an <code>ObjectMapper</code>.
 * 
 * @author Collin McCormack
 */
public class RecordModule extends SimpleModule {

	private static final long serialVersionUID = -4257434704559895522L;

	public RecordModule() {
		super();

		this.addSerializer(Record.class, new JsonRecordSerializer());
		this.addDeserializer(TempDeserCollection.class, new JsonSparkDeserializer());
	}
}
