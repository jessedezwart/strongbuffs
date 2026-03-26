package nl.jessedezwart.strongbuffs.runtime.state.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class GroundItemRuntimeState
{
	private final Map<String, Integer> nearbyGroundItemCounts = new LinkedHashMap<>();

	public void clear()
	{
		nearbyGroundItemCounts.clear();
	}

	public void incrementNearbyGroundItem(String itemName)
	{
		String normalized = InventoryRuntimeState.normalizeName(itemName);

		if (normalized != null)
		{
			nearbyGroundItemCounts.put(normalized, nearbyGroundItemCounts.getOrDefault(normalized, 0) + 1);
		}
	}

	public void decrementNearbyGroundItem(String itemName)
	{
		String normalized = InventoryRuntimeState.normalizeName(itemName);

		if (normalized == null)
		{
			return;
		}

		int count = nearbyGroundItemCounts.getOrDefault(normalized, 0);

		if (count <= 1)
		{
			nearbyGroundItemCounts.remove(normalized);
			return;
		}

		nearbyGroundItemCounts.put(normalized, count - 1);
	}

	public boolean hasNearbyGroundItem(String itemName)
	{
		String normalized = InventoryRuntimeState.normalizeName(itemName);
		return normalized != null && nearbyGroundItemCounts.getOrDefault(normalized, 0) > 0;
	}

	public Map<String, Integer> getNearbyGroundItemCounts()
	{
		return Collections.unmodifiableMap(nearbyGroundItemCounts);
	}
}
