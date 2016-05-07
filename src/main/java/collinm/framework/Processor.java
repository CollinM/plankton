package collinm.framework;

import collinm.framework.data.Record;

public abstract class Processor implements IProcessor {

	/**
	 * Convenience method for the <code>Pipeline</code> to be able to use map
	 * without every <code>Processor</code>'s <code>process</code> method having
	 * to return the original <code>Record</code>.
	 * 
	 * @param r
	 *            the target <code>Record</code>
	 * @return the processed <code>Record</code>
	 */
	protected Record map_process(Record r) {
		this.process(r);
		return r;
	}

	public void close() {};
}
