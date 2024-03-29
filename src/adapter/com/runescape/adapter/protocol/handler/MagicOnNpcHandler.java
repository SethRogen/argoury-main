package com.runescape.adapter.protocol.handler;

import org.apache.mina.core.session.IoSession;

import com.runescape.Static;
import com.runescape.logic.npc.NPC;
import com.runescape.logic.player.Player;
import com.runescape.network.Frame;
import com.runescape.network.handler.PlayerFrameHandler;

public class MagicOnNpcHandler extends PlayerFrameHandler {

	@Override
	public void handleFrame(Player player, IoSession session, Frame frame) {
		// TODO Auto-generated method stub
		int npcIndex = frame.readShort();
		frame.readLEShortA();
		frame.readLEShort();
		int interfaceSettings = frame.readInt1();
		int interfaceId = interfaceSettings >> 16;
		int interfaceButton = interfaceSettings & 0xff;
		frame.readUnsignedS();
		
		NPC npc = Static.world.getNPCs().get(npcIndex);
		
		player.getCombat().createNewCombatAction(player, npc, true, interfaceId, interfaceButton);
		
	}

}
