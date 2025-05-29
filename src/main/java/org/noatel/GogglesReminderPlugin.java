package org.noatel;

import javax.inject.Inject;
import net.runelite.api .*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util .*;

@PluginDescriptor(
        name = "Goggles Reminder",
        description = "Reminds you to wear Prescription Goggles while making potions",
        tags = {"herblore", "goggles", "reminder"}
)
public class GogglesReminderPlugin extends Plugin {
    @Inject
    private Client client;

    // Goggles item ID
    private static final int GOGGLES_ITEM_ID = ItemID.PRESCRIPTION_GOGGLES;

	// List of finished potion item IDs
    private static final Set<Integer> FINISHED_POTIONS = PotionItemIds.POTIONS;

    // Previous inventory state
    private Item[] previousInventory = null;

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {

        // Only handle changes in the player's inventory
        if (event.getContainerId() != InventoryID.INVENTORY.getId())
            return;

        // Get the current inventory items
        Item[] currentInventory = event.getItemContainer().getItems();

        // If this is the first run, just save the current inventory and return
        if (previousInventory != null && isPotionCreated(previousInventory, currentInventory)) {
            // Check if the player is wearing the goggles
            if (!isWearingGoggles()) {
                // Notify the player to wear their Prescription Goggles
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>Don't forget your Prescription Goggles!</col>", null);
            }
        }

        // Save the current inventory for comparison next time
        previousInventory = Arrays.copyOf(currentInventory, currentInventory.length);
    }

    private boolean isWearingGoggles() {
        // Check if the player is wearing Prescription Goggles in the equipment slot
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

        if (equipment == null)
            return false;

        // Check the head slot for the goggles
        Item headItem = equipment.getItem(EquipmentInventorySlot.HEAD.getSlotIdx());
        return headItem != null && headItem.getId() == GOGGLES_ITEM_ID;
    }

    private boolean isPotionCreated(Item[] oldInv, Item[] newInv) {
        Map<Integer, Integer> oldCounts = toItemCountMap(oldInv);
        Map<Integer, Integer> newCounts = toItemCountMap(newInv);

        // Check if any finished potion was created
        for (int itemId : FINISHED_POTIONS) {
            // Get the counts of the item in both inventories
            int oldQty = oldCounts.getOrDefault(itemId, 0);
            int newQty = newCounts.getOrDefault(itemId, 0);

            if (newQty > oldQty) {
                return true; // A potion was made
            }
        }

        return false;
    }

    private Map<Integer, Integer> toItemCountMap(Item[] items) {
        // Create a map to hold item IDs and their quantities
        Map<Integer, Integer> map = new HashMap<>();

        // Iterate through the items and count their quantities
        for (Item item : items) {
            if (item != null && item.getId() != -1) {
                map.put(item.getId(), map.getOrDefault(item.getId(), 0) + item.getQuantity());
            }
        }
        return map;
    }
}
