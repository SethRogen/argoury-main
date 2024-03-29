/* SimpleMap Copyright (C) 1999-2002 Jochen Hoenicke.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; see the file COPYING.LESSER.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * $Id: SimpleMap.java.in,v 1.1.2.1 2002/05/28 17:34:24 hoenicke Exp $
 */

package com.runescape.jode.util;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This is a very simple map, using a set as backing.
 * The default backing set is a simple set, but you can specify any other
 * set of Map.Entry in the constructor.
 */
public class SimpleMap extends AbstractMap {
    private Set backing;

    public SimpleMap() {
        backing = new SimpleSet();
    }

    public SimpleMap(int initialCapacity) {
        backing = new SimpleSet(initialCapacity);
    }

    public SimpleMap(Set fromSet) {
        backing = fromSet;
    }

    public Set entrySet() {
        return backing;
    }

    public static class SimpleEntry implements Map.Entry {
        Object key;
        Object value;

        public SimpleEntry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object newValue) {
            Object old = value;
            value = newValue;
            return old;
        }

        public int hashCode() {
            return key.hashCode() ^ value.hashCode();
        }

        public boolean equals(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry e = (Map.Entry) o;
                return key.equals(e.getKey()) && value.equals(e.getValue());
            }
            return false;
        }
    }

    public Object put(Object key, Object value) {
        for (Iterator i = backing.iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            if (key.equals(entry.getKey()))
                return entry.setValue(value);
        }
        backing.add(new SimpleEntry(key, value));
        return null;
    }
}


