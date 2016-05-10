package collinm.framework;

import java.util.stream.Stream;

import collinm.framework.data.Record;

/**
 * Similar to <code>Processor</code>, but used for data augmentation and so
 * returns a <code>List</code> of <code>Record</code> objects instead of just
 * one.
 * 
 * @author Collin McCormack
 */
public interface IExpander {
	public Stream<Record> expand(Record r);
}
