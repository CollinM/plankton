package collinm.framework.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.mllib.regression.LabeledPoint;

/**
 * "Temporary Deserialized Collection": wrapper class for a <code>List</code> of
 * <code>LabeledPointWithId</code> objects. Required because of JVM type erasure
 * and the way that Jackson finds appropriate deserializers.
 * 
 * @author Collin McCormack
 */
public class TempDeserCollection {

	public final List<LabeledPoint> points;

	public TempDeserCollection() {
		this.points = new ArrayList<>();
	}

	public void add(LabeledPoint lp) {
		this.points.add(lp);
	}
}
