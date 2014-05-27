package com.shuimin.common.struc.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @author Edwin
 */

@SuppressWarnings("serial")
public class FixedSizeLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
	private final int maxEntries;

	public FixedSizeLinkedHashMap(final int maxEntries) {
		super(maxEntries + 1, 1.0f, true);
		this.maxEntries = maxEntries;
	}
	
	@Override
	public V get(Object key) {
		return super.get(key);
	}

	/**
	 * Returns <tt>true</tt> if this <code>LruCache</code> has more entries than
	 * the maximum specified when it was created.
	 * 
	 * <p>
	 * This method <em>does not</em> modify the underlying <code>Map</code>; it
	 * relies on the implementation of <code>LinkedHashMap</code> to do that,
	 * but that behavior is documented in the JavaDoc for
	 * <code>LinkedHashMap</code>.
	 * </p>
	 * 
	 * @param eldest
	 *            the <code>Entry</code> in question; this implementation
	 *            doesn'val care what it is, since the implementation is only
	 *            dependent on the size of the cache
	 * @return <tt>true</tt> if the oldest
	 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
	 */
	@Override
	protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
		return super.size() > maxEntries;
	}
}
