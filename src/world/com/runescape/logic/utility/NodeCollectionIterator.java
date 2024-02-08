package com.runescape.logic.utility;

import java.util.Iterator;

import com.runescape.logic.Node;

/**
 * @author Lazaro
 */
public class NodeCollectionIterator<N extends Node> implements Iterator<N> {
    private Integer[] indexes;
    private NodeCollection<N> nodeCollection;
    private int position = 0;

    public NodeCollectionIterator(NodeCollection<N> nodeList) {
        this.nodeCollection = nodeList;
        //synchronized (nodeList) {
        this.indexes = nodeList.getIndexes().toArray(new Integer[0]);
        //}
    }

    public boolean hasNext() {
        return position != indexes.length;
    }

    @SuppressWarnings("unchecked")
    public N next() {
        while (true) {
            if (position < indexes.length) {
                Node n = nodeCollection.get(indexes[position++]);
                if (n == null) {
                    continue;
                }
                return (N) n;
            }
            return null;
        }
    }

    public void remove() {
        nodeCollection.remove(indexes[position - 1]);
    }
}