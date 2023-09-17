package com.ziotic.logic.npc.summoning;

import com.ziotic.Static;
import com.ziotic.content.handler.ActionHandlerSystem;
import com.ziotic.content.handler.ButtonHandler;
import com.ziotic.logic.item.InventoryListener;
import com.ziotic.logic.item.ItemContainer;
import com.ziotic.logic.item.ItemDefinition;
import com.ziotic.logic.item.ItemListener;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.item.ItemContainer.StackType;
import com.ziotic.logic.item.ItemListener.ItemEventType;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.player.Player;
import com.ziotic.logic.utility.GameInterface;
import com.ziotic.logic.utility.NodeRunnable;

/**
 * @author 'Mystic Flow
 */
public abstract class BeastOfBurden extends Familiar {

	public static final ItemListener BOB_LISTENER = new ItemListener() {

		@Override
		public void event(ItemContainer container, ItemEventType type, int index) {
			switch (type) {
			case CHANGE:
				Static.proto.sendItems(container.getPlayer(), 30, false, container.array());
				break;
			case FULL:
				container.getPlayer().sendMessage("Your familiar can't carry any more items!");
				break;
			}
		}
	};

	public static final ItemListener INVENTORY_LISTENER = new ItemListener() {

		@Override
		public void event(ItemContainer container, ItemEventType type, int index) {
			switch (type) {
			case CHANGE:
				Static.proto.sendItems(container.getPlayer(), 90, false, container.array());
				break;
			case FULL:
				container.getPlayer().sendMessage("There is not enough space in your inventory.");
				break;
			}
		}
	};

	public static final GameInterface INVENTORY_INTERFACE = new GameInterface(665, GameInterface.DEFAULT_INV_POS, null);

	public static final GameInterface STORE_INTERFACE = new GameInterface(671, null, new NodeRunnable<Player>() {
		@Override
		public void run(Player node) {
			node.getFamiliar().beastOfBurden().container.reorder();
			node.getInventory().removeListener(InventoryListener.INSTANCE);
			node.getInventory().addListener(INVENTORY_LISTENER);
			Static.proto.sendAccessMask(node, 0, 30, 671, 27, 0, 1150);
			Static.proto.sendAccessMask(node, 0, 28, 665, 0, 0, 1150);
			Static.proto.sendInterfaceScript(node, 150, 665 << 16, 90, 4, 7, 0, -1, "Store-1", "Store-5", "Store-10", "Store-All", "Store-X", "", "", "", "");
			Static.proto.sendInterfaceScript(node, 150, 671 << 16 | 27, 30, 6, 5, 0, -1, "Withdraw-1", "Withdraw-5", "Withdraw-10", "Withdraw-All", "Withdraw-X", "", "", "", "");
			Static.proto.sendInterface(node, INVENTORY_INTERFACE);
			node.getFamiliar().beastOfBurden().container.refresh();
			node.getInventory().refresh();
		}
	}, new NodeRunnable<Player>() {
		@Override
		public void run(Player node) {
			node.getInventory().addListener(InventoryListener.INSTANCE);
			node.getInventory().removeListener(INVENTORY_LISTENER);
			node.getInventory().refresh();
		}
	});


	private ItemContainer container;

	public BeastOfBurden(Player owner) {
		super(owner);
	}

	public void createContainer() {
		final NPC npc = this;
		this.container = new ItemContainer(owner, maxBurdenSlots(), StackType.NORMAL) {

			public boolean add(int id, int amount, int preferredIndex, boolean notifyListeners, boolean enforceSlot) {
				ItemDefinition def = ItemDefinition.forId(id);
				// This item is too valuable to trust to this familiar.
				if (id == 1436 || id == 7936) {
					if (def.stackTypes == null)
						def.stackTypes = new int[10]; // to make it stackable
					if (npc.getActualId() == 6818 || npc.getActualId() == 6820 || npc.getActualId() == 7349)
						return super.add(id, amount, preferredIndex, notifyListeners, enforceSlot);
					owner.sendMessage("Only abyssal familiars can carry " + def.name.toLowerCase() + "!");
				}
				if (def.isStackable()) {
					owner.sendMessage("You can't store stackable items!");
					return false;
				}
				return super.add(id, amount, preferredIndex, notifyListeners, enforceSlot);
			}
		};
		container.addListener(BOB_LISTENER);
		container.setListening(true);
	}

	public void open() {
		Static.proto.sendInterface(owner, STORE_INTERFACE);
	}

	public ItemContainer getContainer() {
		return container;
	}
	
	@Override
	public BeastOfBurden beastOfBurden() {
		return this;
	}

	public abstract int maxBurdenSlots();
	
	/**
	 * 
	 * @author ' Mystic Flow
	 */
	public static class BurdenStorage implements ButtonHandler {

		@Override
		public void handleButton(Player player, int opcode, int interfaceId, int button, int slot, int item) {
			BeastOfBurden bob = player.getFamiliar() == null ? null : player.getFamiliar().beastOfBurden();
			if (bob == null)
				return;
			ItemContainer selectedContainer = interfaceId == 665 ? player.getInventory() : bob.container;
			ItemContainer otherContainer =  interfaceId == 665 ? bob.container : player.getInventory();
			if (selectedContainer.get(slot) == null || selectedContainer.get(slot).getId() != item)
				return;
			int amount = 0;
			int totalAmount = selectedContainer.amount(item);
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
				amount = totalAmount;
				break;
			case 5:
				amount = -1;
				break;
			case 10:
				player.sendMessage(ItemDefinition.forId(item).examine);
				return;
			}
			if (amount == 0)
				return;
			if (amount == -1)
				return; // display X
			if (amount > totalAmount)
				amount = totalAmount;
			boolean notifyFull = false;
			int remaining = otherContainer.remaining();
			if (amount > remaining) {
				amount = remaining < 1 ? 1 : remaining;
				notifyFull = remaining > 0;
			}
			if (notifyFull)
				otherContainer.event(ItemEventType.FULL, -1);
			// You cannot trade this item to anyone, even a familiar.
			PossesedItem selectedItem = selectedContainer.get(slot);
			if (otherContainer.add(selectedItem.getId(), amount))
				selectedContainer.remove(selectedItem.getId(), amount, amount == 1 ? slot : -1);
		}

		@Override
		public boolean explicitlyForMembers() {
			return false;
		}

		@Override
		public void load(ActionHandlerSystem system) throws Exception {
			system.registerButtonHandler(new int[] {665, 671} , this);
		}

	}

}
