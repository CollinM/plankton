package collinm.framework;

import collinm.framework.data.Record;

/**
 * Interface for <code>Processor</code>. (I hate the <code>I</code> naming
 * scheme, but meh).
 * 
 * @author Collin McCormack
 */
public interface IProcessor {
	/**
	 * The container for the logic of the <code>Processor</code>.
	 * 
	 * @param r
	 *            the target <code>Record</code>
	 */
	public void process(Record r);
	
	public void close();
}
