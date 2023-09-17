package com.ziotic.content.locations;

import com.ziotic.Constants.Equipment;
import com.ziotic.Static;
import com.ziotic.content.handler.ActionHandlerSystem;
import com.ziotic.content.handler.ItemOnObjectHandler;
import com.ziotic.content.handler.ObjectOptionHandler;
import com.ziotic.content.magictemp.MagicTemp;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.World;
import com.ziotic.logic.item.EquipmentDefinition;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.mask.Graphic;
import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.player.Player;

public class MageBank implements ItemOnObjectHandler, ObjectOptionHandler {
	
	public static void cutWebs(final Player player, final GameObject obj) {
		PossesedItem item = player.getEquipment().get(Equipment.WEAPON_SLOT);
		int animation = EquipmentDefinition.DEFAULT_ANIMATIONS.attackAnimations[player.getCombat().weapon.index];
		if (item != null) {
			EquipmentDefinition def = item.getDefinition().getEquipmentDefinition();
			if (def != null) {
				animation = def.getEquipmentAnimations().attackAnimations[player.getCombat().weapon.index];
			}
		}
		player.doAnimation(animation);
		if (World.getRandom(3) == 0) {
			Tick cut = new Tick(null, 1) {
				@Override
				public boolean execute() {
					Static.world.getObjectManager().remove(obj.getLocation());
					Static.world.getObjectManager().add(734, obj.getLocation(), obj.getType(), obj.getDirection());
					player.sendMessage("You cut the web.");
					return false;
				}
			};
			Tick restore = new Tick(null, 100) {
				@Override
				public boolean execute() {
					Static.world.getObjectManager().remove(obj.getLocation());
					Static.world.getObjectManager().add(733, obj.getLocation(), obj.getType(), obj.getDirection());
					return false;
				}
			};
			player.registerTick(cut);
			player.registerTick(restore);
		} else {
			player.sendMessage("You failed to cut the web.");
		}
	}

	@Override
	public void load(ActionHandlerSystem system) throws Exception {
		system.registerItemOnObjectHandler(new int[][] { {1523, 2558}, {1523, 2557} }, this);
		system.registerObjectOptionHandler(new int[] { 733, 1814, 1815, 2557, 2558, 1773, 5959, 5960 }, this);
	}

	@Override
	public boolean explicitlyForMembers() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void handleItemOnObject(Player player, PossesedItem item,
			int itemIndex, GameObject obj) {
		switch (obj.getId()) {
		case 2558:
		case 2557:
			if (World.getRandom(3) == 0) { 
				Static.world.getDoorManager().useDoor(player, obj, true);
			} else {
				player.sendMessage("You failed to picklock the door.");
			}
			break;
		}
	}

	@Override
	public void handleObjectOption1(final Player player, final GameObject obj) {
		switch (obj.getId()) {
		case 733:
			cutWebs(player, obj);
			break;
		case 5959:
			if (obj.getX() == 3090 && obj.getY() == 3956) {
				player.doAnimation(2140);
				player.registerTick(new Tick(null, 1) {
					@Override
					public boolean execute() {
						Static.world.getObjectManager().remove(obj.getLocation());
						Static.world.getObjectManager().add(5961, obj.getLocation(), obj.getType(), obj.getDirection());
						return false;
					}
				});				
				player.registerTick(new Tick(null, 2) {
					@Override
					public boolean execute() {
						Static.world.getObjectManager().remove(obj.getLocation());
						Static.world.getObjectManager().add(5959, obj.getLocation(), obj.getType(), obj.getDirection());
						MagicTemp.teleport(player, Tile.locate(2539, 4712, 0), 2, 1816, 8941, new Graphic(1577), null, 0, true);
						return false;
					}
				});
			}
			break;
		case 5960:
			if (obj.getX() == 2539 && obj.getY() == 4712) {
				player.doAnimation(2140);			
				player.registerTick(new Tick(null, 1) {
					@Override
					public boolean execute() {
						Static.world.getObjectManager().remove(obj.getLocation());
						Static.world.getObjectManager().add(5961, obj.getLocation(), obj.getType(), obj.getDirection());
						return false;
					}
				});
				player.registerTick(new Tick(null, 2) {
					@Override
					public boolean execute() {
						Static.world.getObjectManager().remove(obj.getLocation());
						Static.world.getObjectManager().add(5960, obj.getLocation(), obj.getType(), obj.getDirection());
						MagicTemp.teleport(player, Tile.locate(3090, 3956, 0), 2, 1816, 8941, new Graphic(1577), null, 0, true);
						return false;
					}
				});	
			}
			break;
		case 1815:
			if (obj.getX() == 3153 && obj.getY() == 3923) {
				player.doAnimation(2140);
				player.registerTick(new Tick(null, 1) {
					@Override
					public boolean execute() {
						Static.world.getObjectManager().remove(obj.getLocation());
						Static.world.getObjectManager().add(5961, obj.getLocation(), obj.getType(), obj.getDirection());
						return false;
					}
				});
				player.registerTick(new Tick(null, 2) {
					@Override
					public boolean execute() {
						Static.world.getObjectManager().remove(obj.getLocation());
						Static.world.getObjectManager().add(1815, obj.getLocation(), obj.getType(), obj.getDirection());
						MagicTemp.teleport(player, Tile.locate(2561, 3311, 0), 2, 1816, 8941, new Graphic(1577), null, 0, true);
						return false;
					}
				});	
			}
			break;
		case 1814:
			if (obj.getX() == 2561 && obj.getY() == 3311) {
				player.doAnimation(2140);
				player.registerTick(new Tick(null, 1) {
					@Override
					public boolean execute() {
						Static.world.getObjectManager().remove(obj.getLocation());
						Static.world.getObjectManager().add(5961, obj.getLocation(), obj.getType(), obj.getDirection());
						return false;
					}
				});
				player.registerTick(new Tick(null, 2) {
					@Override
					public boolean execute() {
						Static.world.getObjectManager().remove(obj.getLocation());
						Static.world.getObjectManager().add(1814, obj.getLocation(), obj.getType(), obj.getDirection());
						MagicTemp.teleport(player, Tile.locate(3153, 3923, 0), 2, 1816, 8941, new Graphic(1577), null, 0, true);
						return false;
					}
				});	
			}
			break;
		}
		
	}

	@Override
	public void handleObjectOption2(Player player, GameObject obj) {
		switch (obj.getId()) {
		case 2558:
		case 2557:
			Static.world.getDoorManager().useDoor(player, obj, player.getInventory().contains(1523));
			break;
		}		
	}

	@Override
	public void handleObjectOption3(Player player, GameObject obj) {
		// TODO Auto-generated method stub
		
	}
	
	public static void handleLever(final Player player, final GameObject obj, final Tile destination) {
		player.doAnimation(2140);
		Runnable lp = new Runnable() {
			
			@Override
			public void run() {
				Static.world.getObjectManager().remove(obj.getLocation());
				Static.world.getObjectManager().add(5961, obj.getLocation(), obj.getType(), obj.getDirection());
			}
			
		};
		Static.world.addGlobalProcess("LP_" + obj.getX() + obj.getY() + obj.getZ(), lp, 1);
		Runnable lpb = new Runnable() {

			@Override
			public void run() {
				Static.world.getObjectManager().remove(obj.getLocation());
				Static.world.getObjectManager().add(obj.getId(), obj.getLocation(), obj.getType(), obj.getDirection());
			}
			
		};
		Static.world.addGlobalProcess("LPB_" + obj.getX() + obj.getY() + obj.getZ(), lpb, 2);
		Runnable teleport = new Runnable() {

			@Override
			public void run() {
				MagicTemp.teleport(player, destination, 2, 1816, 8941, new Graphic(1577), null, 0, true);
			}
			
		};
		player.addSpecificProcess(teleport, 2);	
	}

}
