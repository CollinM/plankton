package collinm.framework.data;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;

public class LabeledPointWithId extends LabeledPoint {

	private static final long serialVersionUID = 8837822507284842942L;
	private String id;

	public LabeledPointWithId(String id, double label, Vector features) {
		super(label, features);
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
