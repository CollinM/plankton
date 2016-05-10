package collinm.framework.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Joiner;

/**
 * Sparse vector backed by a <code>HashMap</code>.
 * 
 * @author Collin McCormack
 */
public class Vector {

	public final static double DEFAULT = 0;

	public int size;
	public Map<Integer, Double> v;

	/**
	 * Create a new, empty <code>Vector</code>.
	 * 
	 * @param size
	 *            Size of the vector
	 */
	public Vector(int size) {
		this.v = new HashMap<>();
		this.size = size;
	}

	/**
	 * Set the index <code>key</code> to <code>value</code>.
	 * 
	 * @param key
	 *            index to assign
	 * @param value
	 *            value to save
	 */
	public void set(int key, double value) {
		if (key >= this.size)
			throw new IllegalArgumentException(key + " cannot be greater than " + this.size);
		this.v.put(key, value);
	}

	/**
	 * Increment the value at index <code>key</code>.
	 * 
	 * @param key
	 *            index of the value to increment
	 */
	public void increment(int key) {
		if (this.v.containsKey(key))
			this.set(key, this.v.get(key) + 1);
		else
			this.set(key, 1);
	}

	/**
	 * Get the value at index <code>key</code>.
	 * 
	 * @param key
	 *            target index
	 * @return value at the target index
	 */
	public double get(int key) {
		return this.v.get(key);
	}

	/**
	 * Create a dense vector, filling in any unset indices with zeroes.
	 * 
	 * @return dense vector
	 */
	public double[] materialize() {
		double[] out = new double[this.size];
		Arrays.fill(out, DEFAULT);
		this.v.keySet().forEach(k -> out[k] = this.v.get(k));
		return out;
	}

	/**
	 * Get an <code>Iterator</code> over the values of this vector, inserting
	 * zeroes for unset indices.
	 * 
	 * @return <code>Iterator</code> over vector values
	 */
	public Iterator<Double> getIterator() {
		return new VectorWithDefaultsIterator(this);
	}

	public String toString() {
		return Joiner.on(", ").join(this.getIterator());
	}

	class VectorWithDefaultsIterator implements Iterator<Double> {

		private Vector vec;
		private int marker;

		public VectorWithDefaultsIterator(Vector v) {
			this.vec = v;
			this.marker = 0;
		}

		@Override
		public boolean hasNext() {
			return this.marker < this.vec.size;
		}

		@Override
		public Double next() {
			return this.vec.v.containsKey(this.marker++) ? this.vec.get(this.marker - 1) : Vector.DEFAULT;
		}

	}
}
