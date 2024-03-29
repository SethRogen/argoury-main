/* DeadCodeAnalysis Copyright (C) 1999-2002 Jochen Hoenicke.
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
 * $Id: DeadCodeAnalysis.java.in,v 4.1.2.1 2002/05/28 17:34:03 hoenicke Exp $
 */

package com.runescape.jode.decompiler;

import java.util.Iterator;

import com.runescape.jode.bytecode.BytecodeInfo;
import com.runescape.jode.bytecode.Handler;
import com.runescape.jode.bytecode.Instruction;

public class DeadCodeAnalysis {

    private final static String REACHABLE = "R";
    private final static String REACHCHANGED = "C";

    private static void propagateReachability(BytecodeInfo code) {
        boolean changed;
        do {
            changed = false;
            for (Iterator iter = code.getInstructions().iterator(); iter.hasNext(); ) {
                Instruction instr = (Instruction) iter.next();
                if (instr.getTmpInfo() == REACHCHANGED) {
                    changed = true;
                    instr.setTmpInfo(REACHABLE);
                    Instruction[] succs = instr.getSuccs();
                    if (succs != null)
                        for (int i = 0; i < succs.length; i++)
                            if (succs[i].getTmpInfo() == null)
                                succs[i].setTmpInfo(REACHCHANGED);
                    if (!instr.doesAlwaysJump() && instr.getNextByAddr() != null)
                        if (instr.getNextByAddr().getTmpInfo() == null)
                            instr.getNextByAddr().setTmpInfo(REACHCHANGED);
                    /*XXX code after jsr reachable iff ret is reachable...*/
                    if (instr.getOpcode() == Opcodes.opc_jsr)
                        if (instr.getNextByAddr().getTmpInfo() == null)
                            instr.getNextByAddr().setTmpInfo(REACHCHANGED);
                }
            }
        } while (changed);
    }

    public static void removeDeadCode(BytecodeInfo code) {
        ((Instruction) code.getInstructions().get(0)).setTmpInfo(REACHCHANGED);
        propagateReachability(code);
        Handler[] handlers = code.getExceptionHandlers();
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < handlers.length; i++) {
                if (handlers[i].catcher.getTmpInfo() == null) {
                    /* check if the try block is somewhere reachable
                  * and mark the catcher as reachable then.
                  */
                    for (Instruction instr = handlers[i].start; instr != null; instr = instr.getNextByAddr()) {
                        if (instr.getTmpInfo() != null) {
                            handlers[i].catcher.setTmpInfo(REACHCHANGED);
                            propagateReachability(code);
                            changed = true;
                            break;
                        }
                        if (instr == handlers[i].end)
                            break;
                    }
                }
            }
        } while (changed);

        for (int i = 0; i < handlers.length; i++) {
            /* A handler is not reachable iff the catcher is not reachable */
            if (handlers[i].catcher.getTmpInfo() == null) {
                /* This is very seldom, so we can make it slow */
                Handler[] newHandlers = new Handler[handlers.length - 1];
                System.arraycopy(handlers, 0, newHandlers, 0, i);
                System.arraycopy(handlers, i + 1, newHandlers, i, handlers.length - (i + 1));
                handlers = newHandlers;
                code.setExceptionHandlers(newHandlers);
                i--;
            } else {
                /* This works! */
                while (handlers[i].start.getTmpInfo() == null)
                    handlers[i].start = handlers[i].start.getNextByAddr();
                while (handlers[i].end.getTmpInfo() == null)
                    handlers[i].end = handlers[i].end.getPrevByAddr();
            }
        }

        /* Now remove the dead code and clean up tmpInfo */
        for (Iterator i = code.getInstructions().iterator(); i.hasNext(); ) {
            Instruction instr = (Instruction) i.next();
            if (instr.getTmpInfo() != null)
                instr.setTmpInfo(null);
            else
                i.remove();
        }
    }
}
