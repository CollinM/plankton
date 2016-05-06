package collinm.framework;

import java.util.List;

/**
 * Similar to <code>Processor</code>, but used for data augmentation and so
 * returns a <code>List</code> of <code>Record</code> objects instead of just
 * one.
 * 
 * @author Collin McCormack
 */
public interface Expander {
	public List<Record> expand(Record r);
}
