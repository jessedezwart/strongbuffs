package nl.jessedezwart.strongbuffs.runtime.tracker.updater;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeTrigger;

@Singleton
public class InventoryStateUpdater
{
	private final ItemNameResolver itemNameResolver;

	@Inject
	public InventoryStateUpdater(ItemNameResolver itemNameResolver)
	{
		this.itemNameResolver = itemNameResolver;
	}

	public void refresh(RuntimeState runtimeState, RuntimeConditionRequirements requirements, Client client)
	{
		if (requirements.hasInventoryTracking())
		{
			refreshInventory(runtimeState, requirements, client.getItemContainer(InventoryID.INV));
		}

		if (requirements.hasEquipmentTracking())
		{
			refreshEquipment(runtimeState, requirements, client.getItemContainer(InventoryID.WORN));
		}
	}

	public EnumSet<RuntimeTrigger> onItemContainerChanged(RuntimeState runtimeState,
		RuntimeConditionRequirements requirements, ItemContainerChanged event)
	{
		EnumSet<RuntimeTrigger> triggers = EnumSet.noneOf(RuntimeTrigger.class);

		if (requirements.hasInventoryTracking() && event.getContainerId() == InventoryID.INV)
		{
			refreshInventory(runtimeState, requirements, event.getItemContainer());
			triggers.add(RuntimeTrigger.INVENTORY);
		}

		if (requirements.hasEquipmentTracking() && event.getContainerId() == InventoryID.WORN)
		{
			refreshEquipment(runtimeState, requirements, event.getItemContainer());
			triggers.add(RuntimeTrigger.EQUIPMENT);
		}

		return triggers;
	}

	private void refreshInventory(RuntimeState runtimeState, RuntimeConditionRequirements requirements,
		ItemContainer itemContainer)
	{
		runtimeState.getInventory().clearInventory();

		if (itemContainer == null)
		{
			return;
		}

		for (Item item : itemContainer.getItems())
		{
			if (item == null || item.getId() < 0 || item.getQuantity() <= 0)
			{
				continue;
			}

			String itemName = itemNameResolver.resolve(item.getId());

			if (!requirements.getInventoryItems().contains(itemName))
			{
				continue;
			}

			int currentCount = runtimeState.getInventory().getInventoryItemCount(itemName);
			runtimeState.getInventory().setInventoryItemCount(itemName, currentCount + item.getQuantity());
		}
	}

	private void refreshEquipment(RuntimeState runtimeState, RuntimeConditionRequirements requirements,
		ItemContainer itemContainer)
	{
		List<String> equippedItems = new ArrayList<>();

		if (itemContainer == null)
		{
			runtimeState.getInventory().setEquippedItems(equippedItems);
			return;
		}

		for (Item item : itemContainer.getItems())
		{
			if (item == null || item.getId() < 0 || item.getQuantity() <= 0)
			{
				continue;
			}

			String itemName = itemNameResolver.resolve(item.getId());

			if (requirements.getEquippedItems().contains(itemName))
			{
				equippedItems.add(itemName);
			}
		}

		runtimeState.getInventory().setEquippedItems(equippedItems);
	}
}
