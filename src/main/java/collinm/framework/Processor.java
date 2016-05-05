package collinm.framework;

import java.io.Serializable;

public interface Processor extends Serializable {
	public Record process(Record r);
}
