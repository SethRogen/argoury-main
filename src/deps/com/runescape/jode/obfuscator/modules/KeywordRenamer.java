/* KeywordRenamer Copyright (C) 1999-2002 Jochen Hoenicke.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * $Id: KeywordRenamer.java.in,v 1.2.2.1 2002/05/28 17:34:17 hoenicke Exp $
 */

package com.runescape.jode.obfuscator.modules;

import java.util.Collection;
import java.util.Iterator;

import com.runescape.jode.obfuscator.Identifier;
import com.runescape.jode.obfuscator.OptionHandler;
import com.runescape.jode.obfuscator.Renamer;

public class KeywordRenamer implements Renamer, OptionHandler {
    String keywords[];
    Renamer backup;

    public KeywordRenamer() {
        keywords = new String[]{"if", "else", "for", "while", "throw", "return", "class", "interface", "implements", "extends", "instanceof", "new", "int", "boolean", "long", "float", "double", "short", "public", "protected", "private", "static", "synchronized", "strict", "transient", "abstract", "volatile", "final",
                /* Not really keywords, but very confusing anyway. */
                "Object", "String", "Thread", "Runnable", "StringBuffer", "Vector"};
        backup = new StrongRenamer();
    }

    public void setOption(String option, Collection values) {
        if (option.startsWith("keywords")) {
            keywords = (String[]) values.toArray(new String[values.size()]);
        } else if (option.startsWith("backup")) {
            if (values.size() != 1)
                throw new IllegalArgumentException("Only one backup is allowed");
            backup = (Renamer) values.iterator().next();
        } else
            throw new IllegalArgumentException("Invalid option `" + option + "'");
    }

    public Iterator generateNames(final Identifier ident) {
        return new Iterator() {
            int pos = 0;
            Iterator backing = null;

            public boolean hasNext() {
                return true;
            }

            public Object next() {
                if (pos < keywords.length)
                    return keywords[pos++];

                if (backing == null)
                    backing = backup.generateNames(ident);

                return backing.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
