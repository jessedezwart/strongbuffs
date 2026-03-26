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
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemPrice;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.impl.InventoryRuntimeState;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeTrigger;

@Singleton
public class InventoryStateUpdater
{
	private final ItemNameResolver itemNameResolver;
	private final ItemManager itemManager;

	@Inject
	public InventoryStateUpdater(ItemNameResolver itemNameResolver, ItemManager itemManager)
	{
		this.itemNameResolver = itemNameResolver;
		this.itemManager = itemManager;
	}

	public void refresh(RuntimeState runtimeState, RuntimeStateWatchlist requirements, Client client)
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

	/**
	 * Refreshes item prices, inventory value, and bank value on full state refresh.
	 */
	public void refreshValues(RuntimeState runtimeState, RuntimeStateWatchlist requirements, Client client)
	{
		if (requirements.hasItemPriceTracking())
		{
			refreshItemPrices(runtimeState, requirements);
		}

		if (requirements.tracksInventoryValue())
		{
			refreshInventoryValue(runtimeState, client.getItemContainer(InventoryID.INV));
		}

		if (requirements.tracksBankValue())
		{
			refreshBankValue(runtimeState, client.getItemContainer(InventoryID.BANK));
		}
	}

	public EnumSet<RuntimeTrigger> onItemContainerChanged(RuntimeState runtimeState, RuntimeStateWatchlist requirements,
			ItemContainerChanged event)
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

	/**
	 * Handles value computation when item containers change.
	 */
	public EnumSet<RuntimeTrigger> onItemContainerValueChanged(RuntimeState runtimeState,
			RuntimeStateWatchlist requirements, ItemContainerChanged event)
	{
		EnumSet<RuntimeTrigger> triggers = EnumSet.noneOf(RuntimeTrigger.class);

		if (requirements.tracksInventoryValue() && event.getContainerId() == InventoryID.INV)
		{
			refreshInventoryValue(runtimeState, event.getItemContainer());
			triggers.add(RuntimeTrigger.INVENTORY);
		}

		if (requirements.tracksBankValue() && event.getContainerId() == InventoryID.BANK)
		{
			refreshBankValue(runtimeState, event.getItemContainer());
			triggers.add(RuntimeTrigger.BANK);
		}

		return triggers;
	}

	private void refreshInventory(RuntimeState runtimeState, RuntimeStateWatchlist requirements,
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

	private void refreshEquipment(RuntimeState runtimeState, RuntimeStateWatchlist requirements,
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

	private void refreshItemPrices(RuntimeState runtimeState, RuntimeStateWatchlist requirements)
	{
		runtimeState.getInventory().clearItemPrices();

		for (String normalizedName : requirements.getItemPrices())
		{
			long price = resolveItemPriceByName(normalizedName);
			runtimeState.getInventory().setItemPrice(normalizedName, price);
		}
	}

	private void refreshInventoryValue(RuntimeState runtimeState, ItemContainer itemContainer)
	{
		runtimeState.getInventory().setInventoryTotalValue(computeContainerValue(itemContainer));
	}

	private void refreshBankValue(RuntimeState runtimeState, ItemContainer itemContainer)
	{
		runtimeState.getInventory().setBankTotalValue(computeContainerValue(itemContainer));
	}

	private long computeContainerValue(ItemContainer itemContainer)
	{
		if (itemContainer == null)
		{
			return 0L;
		}

		long totalValue = 0L;

		for (Item item : itemContainer.getItems())
		{
			if (item == null || item.getId() < 0 || item.getQuantity() <= 0)
			{
				continue;
			}

			int canonicalId = itemManager.canonicalize(item.getId());
			long price = itemManager.getItemPrice(canonicalId);
			totalValue += price * item.getQuantity();
		}

		return totalValue;
	}

	private long resolveItemPriceByName(String normalizedName)
	{
		List<ItemPrice> results = itemManager.search(normalizedName);

		if (results == null || results.isEmpty())
		{
			return 0L;
		}

		String target = InventoryRuntimeState.normalizeName(normalizedName);

		for (ItemPrice result : results)
		{
			String candidateName = InventoryRuntimeState.normalizeName(result.getName());

			if (target != null && target.equals(candidateName))
			{
				return itemManager.getItemPrice(result.getId());
			}
		}

		return itemManager.getItemPrice(results.get(0).getId());
	}
}
