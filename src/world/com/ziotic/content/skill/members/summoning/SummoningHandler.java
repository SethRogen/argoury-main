package com.ziotic.content.skill.members.summoning;

import java.util.HashMap;
import java.util.Map;

import com.ziotic.Static;
import com.ziotic.content.handler.ActionHandlerSystem;
import com.ziotic.content.handler.ButtonHandler;
import com.ziotic.content.handler.ObjectOptionHandler;
import com.ziotic.engine.event.DelayedEvent;
import com.ziotic.logic.dialogue.Conversation;
import com.ziotic.logic.dialogue.Dialogue;
import com.ziotic.logic.dialogue.OptionDialogue;
import com.ziotic.logic.item.ItemContainer;
import com.ziotic.logic.item.ItemDefinition;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.npc.summoning.BeastOfBurden;
import com.ziotic.logic.npc.summoning.Familiar;
import com.ziotic.logic.npc.summoning.FamiliarSpecial;
import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.player.DisplayMode;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;
import com.ziotic.logic.utility.GameInterface;
import com.ziotic.logic.utility.NodeRunnable;

/**
 * 
 * @author 'Mystic Flow
 */
public class SummoningHandler implements ButtonHandler, ObjectOptionHandler {

	public static final GameInterface POUCH_CREATION = new GameInterface(672, new NodeRunnable<Player>() {
		@Override
		public void run(Player node) {
			Static.proto.sendAccessMask(node, 0, 462, 672, 16, 0, 190);
			Static.proto.sendInterfaceScript(node, 757, 672 << 16 | 16, 8, 10, "Infuse<col=FF9040>",  "Infuse-5<col=FF9040>", "Infuse-10<col=FF9040>", "Infuse-All<col=FF9040>", "Infuse-X<col=FF9040>", "List<col=FF9040>", 1, 78);
		}
	});

	public static final GameInterface SCROLL_CREATION = new GameInterface(666, new NodeRunnable<Player>() {
		@Override
		public void run(Player node) {
			Static.proto.sendAccessMask(node, 0, 462, 666, 16, 0, 126);
			Static.proto.sendInterfaceScript(node, 763, 666 << 16 | 16, 8, 10, "Transform<col=FF9040>",  "Transform-5<col=FF9040>", "Transform-10<col=FF9040>", "Transform-All<col=FF9040>", "Transform-X<col=FF9040>", 1, 78);
		}
	});

	public static final int[] LEFT_CLICK_BUTTONS = {
		7, 9, 11, 13, 15, 17, 19, 25
	};

	private static final Map<Integer, Dialogue> dismissDialogue = new HashMap<Integer, Dialogue>();

	static {
		dismissDialogue.put(0, new OptionDialogue() {

			@Override
			public String title() {
				return "Are you sure?";
				//TODO Find proper interface id . return "Are you sure you want to dismiss your familiar?";
			}

			@Override
			public String[] text(Conversation conversation) {
				return new String[] { "Yes.", "No."};
			}

			@Override
			public void handle(Conversation convo, int option) {
				convo.end();
				switch(option) {
				case 0:
					convo.getPlayer().getFamiliar().dismiss();
					break;
				}
			}
		});
	}

	public SummoningHandler() {

	}


	@Override
	public boolean explicitlyForMembers() {
		return false;
	}

	@Override
	public void load(ActionHandlerSystem system) throws Exception {
		system.registerButtonHandler(new int[] {149, 672, 666, 662, 747, 880}, this);
		system.registerObjectOptionHandler(new int[] {29954, 28716 }, this);
	}

	@Override
	public void handleButton(Player player, int opcode, int interfaceId, int button, int b2, int itemId) {
		if (interfaceId == 149 && opcode == 7) {
			SummoningPouch pouch = SummoningPouch.get(itemId);
			if (pouch != null) {
				ItemDefinition def = ItemDefinition.forId(pouch.getPouchId());
				if (def == null || def.levelRequirements == null || def.itemRequirements == null) {
					player.sendMessage("Wtf, this isn't supposed to happen ^,^");
					return;
				}
				if (player.getLevels().getLevel(Levels.SUMMONING) < def.levelRequirements.get(Levels.SUMMONING)) {
					player.sendMessage("You need a Summoning level of " + def.levelRequirements.get(Levels.SUMMONING) + " to summon this familiar!");
					return;
				}
				if (player.getLevels().getCurrentLevel(Levels.SUMMONING) - pouch.summonCost() < 1) {
					player.sendMessage("You do not have the required Summoning energy to summon this familiar.");
					player.sendMessage("You will need to recharge it at an obelisk.");
					return;
				}
				Familiar familiar = pouch.createFamiliar(player);
				if (familiar != null) {
					if (player.getFamiliar() != null) {
						player.sendMessage("You already have a familiar summoned!");
						return;
					}
					familiar.summon(pouch);
				}
			}
			return;
		} else if (interfaceId == 747) {
			if (button == 7) { // display left click
				Static.proto.sendInterface(player, 880, player.getDisplayMode() == DisplayMode.FIXED ? 548 : 746, player.getDisplayMode() == DisplayMode.FIXED ? 219 : 104, false);
				Static.proto.sendInterfaceVariable(player, 168, 95);
				return;
			}
			if (player.getFamiliar() == null) {
				switch (button) {
				case 13: // Renew familiar
					player.sendMessage("You must summon a familiar to renew it's timer!");
					break;
				case 12: // Take BoB
					player.sendMessage("You do not have a familiar, pet or other follower right now.");
					break;
				case 11: // dismiss
					player.sendMessage("You do not have a familiar or pet to dismiss.");
					break;
				}
				return;
			}
			switch (button) { 
			case 18:
			case 9: // show details
				player.getFamiliar().showDetails();
				break;
			case 19:
			case 10: // call familiar
				player.getFamiliar().call(true);
				break;
			case 20:
			case 11: // dismiss
				new Conversation(dismissDialogue, player, null).init();
				break;
			case 21:
			case 12: // Take BoB
				takeBoB(player);
				break;
			case 22:
			case 13: // Renew familiar
				renewFamiliar(player);
				break;
			case 17: // Cast special
				player.getFamiliar().doSpecial(FamiliarSpecial.CLICK, null);
				break;
			}
		} else if (interfaceId == 880) {
			if (button == 21) {
				if (player.getFamiliar() != null) {
					player.getFamiliar().showDetails();
				} else {
					Static.proto.sendCloseInterface(player, 548, 219);
					Static.proto.sendInterfaceShowConfig(player, 747, 8, true);
				}
				Static.proto.sendConfig(player, 1493, player.getAttributes().getInt("leftClickSelection"));
			} else {
				int leftClickSelection = -1;
				for (int i = 0; i < LEFT_CLICK_BUTTONS.length; i++)
					if (button == LEFT_CLICK_BUTTONS[i])
						leftClickSelection = i;
				if (leftClickSelection == -1) 
					leftClickSelection = 0;
				Static.proto.sendConfig(player, 1494, leftClickSelection);
				player.getAttributes().set("leftClickSelection", leftClickSelection);
			}
		} else if (interfaceId == 662) {
			switch (button) { 
			case 49: // call familiar
				player.getFamiliar().call(true);
				break;
			case 51: // dismiss
				new Conversation(dismissDialogue, player, null).init();
				break;
			case 67: // Take BoB
				takeBoB(player);
				break;
			case 69: // Renew familiar
				renewFamiliar(player);
				break;
			}
		} else {
			switch (button) { 
			case 18:
			case 19:
				if (interfaceId == POUCH_CREATION.getId()) {
					Static.proto.sendCloseInterface(player, POUCH_CREATION);
					Static.proto.sendInterface(player, SCROLL_CREATION);
				} else {
					Static.proto.sendCloseInterface(player, SCROLL_CREATION);
					Static.proto.sendInterface(player, POUCH_CREATION);
				}
				return;
			}
			int amount = 0;
			if (interfaceId == POUCH_CREATION.getId()) {
				switch (opcode) {
				case 1:
					amount = 1;
					break;
				case 2: 
					amount = 5;
					break;
				case 3:
					amount = 10;
					break;
				case 4:
					amount = Integer.MAX_VALUE - 1;
					break;
				}
				if (amount > 0) {
					createPouch(player, itemId, amount, player.getAttributes().<GameObject>get("summoningObelisk"));
				}
			}
		}
	}

	public void createPouch(Player player, int itemId, int amount, final GameObject obelisk) {
		if (obelisk == null) {
			return;
		}
		SummoningPouch pouch = SummoningPouch.get(itemId);
		if (pouch == null) {
			pouch = SummoningPouch.forInterfaceId(itemId);
			if (pouch == null) {
				player.sendMessage("This hasn't been added yet! Id-" + itemId);
				return;
			}
			ItemDefinition def = ItemDefinition.forId(pouch.getPouchId());
			if (def == null || def.levelRequirements == null || def.itemRequirements == null) {
				player.sendMessage("Wtf, this isn't supposed to happen ^,^");
				return;
			}
			if (player.getLevels().getLevel(Levels.SUMMONING) < def.levelRequirements.get(Levels.SUMMONING)) {
				player.sendMessage("You need a Summoning level of " + def.levelRequirements.get(Levels.SUMMONING) + " to create this pouch!");
				return;
			}
			for (Map.Entry<Integer, Integer> entry : def.itemRequirements.entrySet()) {
				if (!player.getInventory().contains(entry.getKey(), entry.getValue())) {
					player.sendMessage("You don't have the required items to create this pouch!");
					return;
				}
			}
			return;
		}
		ItemDefinition def = ItemDefinition.forId(itemId, false);
		for (Map.Entry<Integer, Integer> entry : def.itemRequirements.entrySet()) {
			player.getInventory().remove(entry.getKey(), entry.getValue());
		}
		//XXX get animation id ^,^
		Static.proto.sendCloseInterface(player);
		player.getInventory().add(pouch.getPouchId());
		player.getLevels().addXP(Levels.SUMMONING, pouch.getExperience());
		final Player[] players = Static.world.getLocalPlayers(player.getLocation());
		for (Player p : players) {
			Static.proto.sendAnimateObject(p, obelisk, 8509);
		}
		Static.engine.submit(new DelayedEvent(2300) {
			@Override
			public void run() {
				for (Player p : players) {
					Static.proto.sendAnimateObject(p, obelisk, 8510);
				}
			}
		});
	}

	public void takeBoB(Player player) {
		BeastOfBurden bob = player.getFamiliar().beastOfBurden();
		if (bob != null) {
			ItemContainer burden = bob.getContainer();
			if (burden.remaining() == burden.capacity()) {
				player.sendMessage("Your beast of burden has no more items.");
				return;
			}
			for (PossesedItem item : burden.array())
				if (item != null)
					if (!player.getInventory().add(item))
						break;
					else 
						burden.remove(item);
		} else {
			player.sendMessage("This follower can't hold any items.");
		}
	}

	public void renewFamiliar(Player player) {
		int ticks = player.getFamiliar().getTicks();
		int maxTicks = player.getFamiliar().getMinutes() * 100;
		if (maxTicks - 50 <= ticks) {
			player.sendMessage("Your familiar's timer is already at maximum");
		} else if (ticks >= 250) {
			player.sendMessage("It must have less than two and a half minutes to renew.");
		} else {
			SummoningPouch pouch = player.getFamiliar().pouch();
			if (!player.getInventory().contains(pouch.getPouchId())) {
				player.sendMessage("To renew your familiar's timer you must have a pouch of that familiar's type and be");
				player.sendMessage("ready to sacrifice it.");
				return;
			}
			player.getFamiliar().performSummoningCircle();
			player.getFamiliar().setTicks(maxTicks);
			player.getInventory().remove(pouch.getPouchId());
			player.sendMessage("You sacrifice a pouch to renew your familiar.");
		}
	}

	@Override
	public void handleObjectOption1(Player player, GameObject obj) {
		if (obj.getDefinition().actions[0] != null && obj.getDefinition().actions[0].equalsIgnoreCase("renew-points")) {
			if (player.getLevels().getCurrentLevel(Levels.SUMMONING) < player.getLevels().getLevel(Levels.SUMMONING)) {
				player.doAnimation(8502);
				player.doGraphics(1308);
				player.sendMessage("You have recharged your Summoning points.");
				player.getLevels().setCurrentLevel(Levels.SUMMONING, player.getLevels().getLevel(Levels.SUMMONING));
				Static.proto.sendLevel(player, Levels.SUMMONING);
			} else {
				player.sendMessage("You already have full Summoning points.");
			}
		} else {
			Static.proto.sendInterface(player, POUCH_CREATION);
			player.getAttributes().set("summoningObelisk", obj);
		}
	}

	@Override
	public void handleObjectOption2(Player player, GameObject obj) {

	}

	@Override
	public void handleObjectOption3(Player player, GameObject obj) {

	}

}
