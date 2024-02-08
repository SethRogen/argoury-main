package com.runescape.adapter.protocol.handler;

import com.runescape.Static;
import com.runescape.logic.object.ObjectDefinition;
import com.runescape.logic.player.Player;
import com.runescape.network.Frame;
import com.runescape.network.handler.PlayerFrameHandler;

import org.apache.mina.core.session.IoSession;

public class ObjectExamineHandler extends PlayerFrameHandler {

    @Override
    public void handleFrame(Player player, IoSession session, Frame frame) {
        int id = frame.readUnsignedShort();
        ObjectDefinition def = ObjectDefinition.forId(id);
        if (def != null)
            Static.proto.sendMessage(player, "Examine object [" + id + "] solid: " + def.walkable);
    }

}
