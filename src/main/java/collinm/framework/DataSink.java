package collinm.framework;

import java.io.Closeable;
import java.io.IOException;

/**
 * Provides a mechanism for recording <code>Record</code> objects.
 * 
 * @author Collin McCormack
 */
public interface DataSink extends Closeable {

	/**
	 * Store a <code>Record</code>.
	 * 
	 * @param r
	 *            the target <code>Record</code>
	 * @throws IOException
	 */
	public void writeRecord(Record r) throws IOException;
}
