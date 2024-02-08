package com.runescape.content.combat.misc;

import java.util.LinkedHashSet;
import java.util.Set;

import com.runescape.engine.tick.Tick;
import com.runescape.logic.map.Coverage;
import com.runescape.logic.map.Tile;
import com.runescape.logic.map.event.NPCTileEventListener;
import com.runescape.logic.map.event.PlayerTileEventListener;
import com.runescape.logic.npc.NPC;
import com.runescape.logic.npc.NPCDefinition;
import com.runescape.logic.npc.NPC.MovementType;
import com.runescape.logic.player.Player;
import com.runescape.utility.Destroyable;

public class AggressionHandler implements NPCTileEventListener, PlayerTileEventListener, Destroyable {
	
	private Coverage coverage;
	private NPC owner;
	private Set<Tile> listeners = new LinkedHashSet<Tile>();
	
	public AggressionHandler(NPC owner) {
		this.owner = owner;
		NPCDefinition def = owner.getDefinition();
		Tile center = owner.getCoverage().center();
		this.coverage = new Coverage(center.translate(-def.aggressiveRange, -def.aggressiveRange, center.getZ()), def.aggressiveRange * 2);		
	}
	
	public void register() {
		Tile[][] tiles = coverage.tiles();
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[x].length; y++) {
				register(tiles[x][y]);
			}
		}
	}
	
	public void unregister() {
		for (Tile t : listeners) {
			unregister(t);
		}
		owner = null;
	}

	@Override
	public void onPlayerEvent(Player player, Tile tile) {
		if (owner == null) {
			unregister(tile);
			return;
		}
		Tick t = owner.retrieveTick("CombatTick");
		if (owner.getCombat().scheduledAction != null && t != null) {
			if (owner.getCombat().scheduledAction.victim == player)
				return;
			else if (owner.getCombat().isAttacking())
				return;
			
		}
		if (t == null) {
			owner.getCombat().createNewCombatAction(owner, player);
		} else if (/*!t.running() || */owner.getCombat().scheduledAction == null) {
			owner.getCombat().createNewCombatAction(owner, player);
		} else if (!owner.getCombat().isAttacking() && owner.getMovementType() == MovementType.RANDOM) {
			owner.getCombat().createNewCombatAction(owner, player);
		} else if(!t.running()) {
			owner.getCombat().createNewCombatAction(owner, player);
		}
	}

	@Override
	public void onNPCEvent(NPC npc, Tile tile) {
	}

	@Override
	public void onRegister(Tile tile) {
		if (tile.containsPlayers()) {
			for (Player player : tile.getPlayers()) {
				if (player != null) {
					onPlayerEvent(player, tile);
				}
			}
		}		
	}

	@Override
	public void destroy() {
		unregister();
	}
	
	private void register(Tile tile) {
		tile.registerEventListener(this);
		listeners.add(tile);
	}
	
	private void unregister(Tile tile) {
		tile.unregisterEventListener(this);
	}

}
