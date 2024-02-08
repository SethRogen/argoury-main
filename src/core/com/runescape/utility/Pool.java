package com.runescape.utility;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Lazaro
 */
public class Pool<T extends Poolable> {
    private Initiator<T> initiator;
    private Queue<T> objectPool;
    private Class<?> parent;

    /**
     * Initiates a new object pool.
     *
     * @param parent    The class of the objects being pooled.
     * @param initiator The initiator to setup the pooled objects.
     * @param size      The initial count of objects cached.
     */
    @SuppressWarnings("unchecked")
    public Pool(Class<?> parent, Initiator<T> initiator, int size) {
        this.parent = parent;
        this.objectPool = new LinkedList<T>();
        this.initiator = initiator;
        try {
            for (int i = size; i != 0; i--) {
                T object = (T) parent.newInstance();
                if (initiator != null)
                    initiator.init(object);
                objectPool.offer(object);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a new object that isn't being used.
     * <p/>
     * If the pool is currently empty, it will create a new object and add it to
     * the pool.
     *
     * @return An object that isn't being used.
     */
    @SuppressWarnings("unchecked")
    public T acquire() {
        T object;
        synchronized (objectPool) {
            while ((object = objectPool.poll()) != null) {
                if (!object.expired()) {
                    break;
                }
            }
        }
        if (object == null) {
            try {
                object = (T) parent.newInstance();
                if (initiator != null)
                    initiator.init(object);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return object;
    }

    /**
     * Gets the number of available cached objects.
     *
     * @return The number of available cached objects.
     */
    public int available() {
        return objectPool.size();
    }

    /**
     * Releases an object and prepares it for reuse.
     *
     * @param object The object to be recycled.
     */
    public void release(T object) {
        if (!object.expired()) {
            object.recycle();
            synchronized (objectPool) {
                objectPool.add(object);
            }
        }
    }

    /**
     * Gets the total number of objects cached by the pool.
     *
     * @return The total number of objects cached by the pool.
     */
    public int size() {
        return objectPool.size();
    }
}