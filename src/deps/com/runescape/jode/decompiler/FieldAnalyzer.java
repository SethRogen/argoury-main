/* FieldAnalyzer Copyright (C) 1998-2002 Jochen Hoenicke.
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
 * $Id: FieldAnalyzer.java.in,v 4.3.2.3 2002/05/28 17:34:03 hoenicke Exp $
 */

package com.runescape.jode.decompiler;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Set;

import com.runescape.jode.bytecode.FieldInfo;
import com.runescape.jode.expr.ConstOperator;
import com.runescape.jode.expr.Expression;
import com.runescape.jode.expr.OuterLocalOperator;
import com.runescape.jode.type.Type;

public class FieldAnalyzer implements Analyzer {
    ClassAnalyzer clazz;
    ImportHandler imports;
    int modifiers;
    Type type;
    String fieldName;
    Expression constant;
    boolean isSynthetic;
    boolean isDeprecated;
    boolean analyzedSynthetic = false;

    public FieldAnalyzer(ClassAnalyzer cla, FieldInfo fd, ImportHandler i) {
        clazz = cla;
        imports = i;

        modifiers = fd.getModifiers();
        type = Type.tType(fd.getType());
        fieldName = fd.getName();
        constant = null;
        this.isSynthetic = fd.isSynthetic();
        this.isDeprecated = fd.isDeprecated();
        if (fd.getConstant() != null) {
            constant = new ConstOperator(fd.getConstant());
            constant.setType(type);
            constant.makeInitializer(type);
        }
    }

    public String getName() {
        return fieldName;
    }

    public Type getType() {
        return type;
    }

    public ClassAnalyzer getClassAnalyzer() {
        return clazz;
    }

    public Expression getConstant() {
        return constant;
    }

    public boolean isSynthetic() {
        return isSynthetic;
    }

    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    public void analyzedSynthetic() {
        analyzedSynthetic = true;
    }

    public boolean setInitializer(Expression expr) {
        if (constant != null)
            return constant.equals(expr);

        /* This should check for isFinal(), but sadly, sometimes jikes
       * doesn't make a val$ field final.  I don't know when, or why,
       * so I currently ignore isFinal.
       */
        if (isSynthetic && (fieldName.startsWith("this$") || fieldName.startsWith("val$"))) {
            if (fieldName.startsWith("val$") && fieldName.length() > 4 && expr instanceof OuterLocalOperator) {
                LocalInfo li = ((OuterLocalOperator) expr).getLocalInfo();
                li.addHint(fieldName.substring(4), type);
            }
            analyzedSynthetic();
        } else
            expr.makeInitializer(type);

        constant = expr;
        return true;
    }

    public boolean setClassConstant(String clazzName) {
        if (constant != null)
            return false;
        if (clazzName.charAt(0) == '[') {
            if (clazzName.charAt(clazzName.length() - 1) == ';')
                clazzName = clazzName.substring(0, clazzName.length() - 1);

            if (fieldName.equals("array" + (clazzName.replace('[', '$').replace('.', '$')))) {
                analyzedSynthetic();
                return true;
            }
        } else {
            if (fieldName.equals("class$" + clazzName.replace('.', '$')) || fieldName.equals("class$L" + clazzName.replace('.', '$'))) {
                analyzedSynthetic();
                return true;
            }
        }
        return false;
    }

    public void analyze() {
        imports.useType(type);
    }

    public void makeDeclaration(Set done) {
        if (constant != null) {
            constant.makeDeclaration(done);
            constant = constant.simplify();
        }
    }

    public boolean skipWriting() {
        return analyzedSynthetic;
    }

    public void dumpSource(TabbedPrintWriter writer) throws IOException {
        if (isDeprecated) {
            writer.println("/**");
            writer.println(" * @deprecated");
            writer.println(" */");
        }
        if (isSynthetic)
            writer.print("/*synthetic*/ ");
        int modifiedModifiers = modifiers;
        /*
       * JLS-1.0, section 9.3:
       *
       * Every field declaration in the body of an interface is
       * implicitly public, static, and final. It is permitted, but
       * strongly discouraged as a matter of style, to redundantly
       * specify any or all of these modifiers for such fields.
       *
       * But I personally don't like this style..., move the
       * comment mark if you think different.

       if (clazz.getClazz().isInterface())
           modifiedModifiers &= ~(Modifier.PUBLIC
                      | Modifier.STATIC
                      | Modifier.FINAL);
       */
        writer.startOp(writer.NO_PAREN, 0);
        String modif = Modifier.toString(modifiedModifiers);
        if (modif.length() > 0)
            writer.print(modif + " ");

        writer.printType(type);
        writer.print(" " + fieldName);
        if (constant != null) {
            writer.breakOp();
            writer.print(" = ");
            constant.dumpExpression(writer.IMPL_PAREN, writer);
        }
        writer.endOp();
        writer.println(";");
    }

    public String toString() {
        return getClass().getName() + "[" + clazz.getClazz() + "." + getName() + "]";
    }
}

