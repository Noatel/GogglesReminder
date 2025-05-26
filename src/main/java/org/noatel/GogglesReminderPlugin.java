package org.noatel;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "Goggles Reminder",
		description = "Reminds you to wear Prescription Goggles while making potions",
		tags = {"herblore", "goggles", "reminder"}
)
public class GogglesReminderPlugin extends Plugin
{
	private static final int GOGGLES_ITEM_ID = ItemID.PRESCRIPTION_GOGGLES;

	@Inject
	private Client client;

	@Inject
	private Notifier notifier;

	@Inject
	private GogglesReminderConfig config;

	private int potionsMade = 0;
	private int lastXp = -1;

	@Provides
	GogglesReminderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GogglesReminderConfig.class);
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.HERBLORE)
			return;

		int xp = event.getXp();
		if (lastXp != -1 && xp > lastXp)
		{
			potionsMade++;
		}
		lastXp = xp;
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		// For every couple potions made, remind the player to wear goggles
		if (potionsMade >= 2)
		{
			Item headItem = client.getItemContainer(InventoryID.EQUIPMENT).getItem(EquipmentInventorySlot.HEAD.getSlotIdx());

			boolean wearingGoggles = headItem != null && headItem.getId() == GOGGLES_ITEM_ID;
			if (!wearingGoggles)
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>Don't forget your Prescription Goggles!</col>", null);
			}

			potionsMade = 0;
		}
	}
}
