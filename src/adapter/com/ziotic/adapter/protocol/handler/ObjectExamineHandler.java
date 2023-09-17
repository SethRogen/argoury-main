package com.ziotic.adapter.protocol.handler;

import com.ziotic.Static;
import com.ziotic.logic.object.ObjectDefinition;
import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;
import com.ziotic.network.handler.PlayerFrameHandler;
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
