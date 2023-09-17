/**
 *
 */
package com.ziotic.utility;

import java.util.Comparator;
import java.util.TreeMap;

/**
 * @author Lazaro
 */
public class CachedMap<K, V> extends TreeMap<K, V> {
    private static final long serialVersionUID = -972646765794714920L;

    public static final int DEFAULT_MAX_SIZE = 256;

    private int maxSize;

    public CachedMap() {
        this(DEFAULT_MAX_SIZE);
    }

    public CachedMap(int maxSize) {
        this(maxSize, null);
    }

    public CachedMap(int maxSize, Comparator<? super K> comparator) {
        super(comparator);
        this.maxSize = maxSize;
    }

    private void evict() {
        remove(firstKey());
    }

    @Override
    public V put(K key, V value) {
        if (size() >= maxSize) {
            evict();
        }

        return super.put(key, value);
    }
}
