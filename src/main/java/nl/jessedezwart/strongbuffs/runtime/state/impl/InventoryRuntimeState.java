package nl.jessedezwart.strongbuffs.runtime.state.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class InventoryRuntimeState
{
	private final Map<String, Integer> inventoryItemCounts = new LinkedHashMap<>();
	private final Set<String> equippedItemNames = new LinkedHashSet<>();

	public Map<String, Integer> getInventoryItemCounts()
	{
		return Collections.unmodifiableMap(inventoryItemCounts);
	}

	public void clearInventory()
	{
		inventoryItemCounts.clear();
	}

	public void setInventoryItemCount(String itemName, int count)
	{
		String normalized = normalizeName(itemName);

		if (normalized == null)
		{
			return;
		}

		if (count <= 0)
		{
			inventoryItemCounts.remove(normalized);
			return;
		}

		inventoryItemCounts.put(normalized, count);
	}

	public int getInventoryItemCount(String itemName)
	{
		String normalized = normalizeName(itemName);
		return normalized == null ? 0 : inventoryItemCounts.getOrDefault(normalized, 0);
	}

	public boolean hasInventoryItem(String itemName)
	{
		return getInventoryItemCount(itemName) > 0;
	}

	public void setEquippedItems(Collection<String> itemNames)
	{
		equippedItemNames.clear();
		addNormalizedNames(equippedItemNames, itemNames);
	}

	public Set<String> getEquippedItemNames()
	{
		return Collections.unmodifiableSet(equippedItemNames);
	}

	public boolean hasEquippedItem(String itemName)
	{
		String normalized = normalizeName(itemName);
		return normalized != null && equippedItemNames.contains(normalized);
	}

	public void clear()
	{
		inventoryItemCounts.clear();
		equippedItemNames.clear();
	}

	private static void addNormalizedNames(Set<String> target, Collection<String> names)
	{
		if (names == null)
		{
			return;
		}

		for (String name : names)
		{
			String normalized = normalizeName(name);

			if (normalized != null)
			{
				target.add(normalized);
			}
		}
	}

	public static String normalizeName(String itemName)
	{
		if (itemName == null)
		{
			return null;
		}

		String normalized = itemName.trim().toLowerCase(Locale.ROOT);
		return normalized.isEmpty() ? null : normalized;
	}
}
