package collinm.framework;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Record implements Serializable {

	private static final long serialVersionUID = 1L;
	private final static String ID_KEY = "ID";
	private Map<String, Object> attributes;
	
	public Record(String id) {
		this.attributes = new HashMap<>();
		this.set(ID_KEY, id);
	}
	
	public Record set(String key, Object value) {
		this.attributes.put(key, value);
		return this;
	}
	
	public Object get(String key) {
		return this.attributes.containsKey(key) ? this.attributes.get(key) : null;
	}
	
	public String id() {
		return (String) this.attributes.get(ID_KEY);
	}
}
