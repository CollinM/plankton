package collinm.framework;

import java.util.Iterator;

import collinm.framework.data.Record;

/**
 * Provides <code>Record</code> objects for a <code>Pipeline</code>.
 * 
 * @author Collin McCormack
 */
public interface DataSource extends Iterator<Record> {
}
