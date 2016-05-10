package collinm.framework.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * General instance of data.
 * 
 * @author Collin McCormack
 */
public class Record {

	public final static String ID_KEY = "ID";
	private Map<String, Object> attributes;
	private Map<String, Class<?>> attributeTypes;

	public Record() {
		this.attributes = new HashMap<>();
		this.attributeTypes = new HashMap<>();
	}
	
	public Record(String id) {
		this();
		this.set(ID_KEY, id);
	}

	/**
	 * Set the attribute <code>key</code> with <code>value</code>.
	 * 
	 * @param key
	 *            key to store at
	 * @param value
	 *            value to store
	 * @return the <code>Record</code> object, to allow for convenient chaining
	 */
	public Record set(String key, Object value) {
		this.attributes.put(key, value);
		this.attributeTypes.put(key, value.getClass());
		return this;
	}

	/**
	 * Get the value stored at <code>key</code>
	 * 
	 * @param key
	 *            target key
	 * @return value stored at <code>key</code>
	 */
	public Object get(String key) {
		return this.attributes.containsKey(key) ? this.attributes.get(key) : null;
	}

	public Class<?> getType(String key) {
		return this.attributeTypes.get(key);
	}

	/**
	 * Get all of the keys in this <code>Record</code>.
	 * 
	 * @return all of the keys in this <code>Record</code>, unsorted
	 */
	public List<String> getKeys() {
		return new ArrayList<String>(this.attributes.keySet());
	}

	/**
	 * Get the ID of the <code>Record</code>.
	 * 
	 * @return <code>Record</code> ID
	 */
	public String id() {
		return (String) this.attributes.get(ID_KEY);
	}
	
	public Record copy(String suffix) {
		Record copy = new Record(this.id() + suffix);
		for (Entry<String, Object> pair : this.attributes.entrySet())
			copy.set(pair.getKey(), pair.getValue());
		return copy;
	}
}
