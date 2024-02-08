package com.runescape.logic.utility;

import java.lang.reflect.Array;
import java.util.*;

import com.runescape.logic.Node;

/**
 * @author Lazaro
 */
public class NodeCollection<N extends Node> extends AbstractCollection<N> {
    private int currentIndex;
    private List<Integer> indexes = Collections.synchronizedList(new LinkedList<Integer>());
    private boolean indexOptimization = true;
    private int minimumIndex, maximumIndex;
    private Object[] nodes;

    public NodeCollection() {
        this(0, 1024);
    }

    public NodeCollection(int maximumIndex) {
        this(0, maximumIndex);
    }

    public NodeCollection(int minimumIndex, int maximumIndex) {
        this.minimumIndex = minimumIndex;
        this.maximumIndex = maximumIndex;
        nodes = new Object[maximumIndex];
        currentIndex = minimumIndex - 1;
    }

    @Override
    public boolean add(N node) {
        return add(node, nextIndex());
    }

    /**
     * Adds an index node onto the list at the specified position.
     *
     * @param node  The object node.
     * @param index The position.
     * @return If the index node was added.
     */
    public boolean add(N node, int index) {
        if (index == -1) {
            return false;
        }
        if (index > maximumIndex || index < minimumIndex) {
            throw new IndexOutOfBoundsException("Index out of bounds : " + index);
        }
        if (nodes[index] != null) {
            return false;
        }
        nodes[index] = node;
        indexes.add(index);
        node.setIndex(index);
        node.setValid(true);
        return true;
    }

    public int capacity() {
        return maximumIndex - minimumIndex;
    }

    @Override
    public boolean contains(Object object) {
        if (object instanceof Node) {
            Node node = (Node) object;
            if (node.getIndex() != -1 && node.getIndex() >= minimumIndex && node.getIndex() <= maximumIndex) {
                return node.equals(nodes[node.getIndex()]);
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public N get(int index) {
        return (N) nodes[index];
    }

    public List<Integer> getIndexes() {
        return indexes;
    }

    public int getMaximumIndex() {
        return maximumIndex;
    }

    public int getMinimumIndex() {
        return minimumIndex;
    }

    public int indexOf(Object object) {
        if (contains(object)) {
            return ((Node) object).getIndex();
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return indexes.isEmpty();
    }

    public boolean isFull() {
        return indexes.size() >= maximumIndex;
    }

    @Override
    public Iterator<N> iterator() {
        return new NodeCollectionIterator<N>(this);
    }

    protected int nextIndex() {
        if (indexOptimization) {
            currentIndex++;
            if (currentIndex > maximumIndex) {
                currentIndex = minimumIndex;
            }
        }
        if (!indexOptimization || nodes[currentIndex] != null) {
            currentIndex = -1;
            for (int i = minimumIndex; i <= maximumIndex; i++) {
                if (nodes[i] == null) {
                    currentIndex = i;
                    break;
                }
            }
        }
        return currentIndex;
    }

    @SuppressWarnings("unchecked")
    public N remove(int index) {
        if (index > maximumIndex || index < minimumIndex) {
            throw new IndexOutOfBoundsException("Index out of bounds : " + index);
        }
        N node = (N) nodes[index];
        if (node != null) {
            nodes[index] = null;
            indexes.remove((Integer) index);
            node.setValid(false);
        }
        return node;
    }

    public boolean remove(N node) {
        if (contains(node)) {
            return remove(node.getIndex()) != null;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object object) {
        if (object instanceof Node) {
            return remove((N) object);
        }
        return false;
    }

    /**
     * If true, this collection will optimize indexing object nodes to save
     * time. However, when enabled, the list will not be ordered properly.
     *
     * @param indexOptimization
     */
    public void setIndexOptimization(boolean indexOptimization) {
        this.indexOptimization = indexOptimization;
    }

    @Override
    public int size() {
        return indexes.size();
    }

    @Override
    public Object[] toArray() {
        return nodes;
    }

    @SuppressWarnings({"unchecked", "hiding"})
    @Override
    public <N> N[] toArray(N[] a) {
        N[] r = a;
        if (nodes.length > r.length) {
            r = (N[]) Array.newInstance(a.getClass().getComponentType(), nodes.length);
        }
        System.arraycopy(nodes, 0, r, 0, nodes.length);
        return r;
    }

    public List<N> toList() {
        List<N> list = new ArrayList<N>(size());
        for (Iterator<N> it = iterator(); it.hasNext(); ) {
            list.add(it.next());
        }
        return list;
    }
}