package nl.jessedezwart.strongbuffs.runtime.tracker.updater;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

@Singleton
public class GroundItemStateUpdater
{
	private final ItemNameResolver itemNameResolver;

	@Inject
	public GroundItemStateUpdater(ItemNameResolver itemNameResolver)
	{
		this.itemNameResolver = itemNameResolver;
	}

	public void refresh(RuntimeState runtimeState, RuntimeConditionRequirements requirements, Client client)
	{
		runtimeState.getGroundItems().clear();

		if (!requirements.hasGroundItemTracking())
		{
			return;
		}

		Tile[][][] tiles = client.getTopLevelWorldView().getScene().getTiles();

		if (tiles == null)
		{
			return;
		}

		for (Tile[][] planeTiles : tiles)
		{
			if (planeTiles == null)
			{
				continue;
			}

			for (Tile[] row : planeTiles)
			{
				if (row == null)
				{
					continue;
				}

				for (Tile tile : row)
				{
					if (tile == null)
					{
						continue;
					}

					for (TileItem item : tile.getGroundItems())
					{
						String itemName = itemNameResolver.resolve(item.getId());

						if (requirements.getGroundItems().contains(itemName))
						{
							runtimeState.getGroundItems().incrementNearbyGroundItem(itemName);
						}
					}
				}
			}
		}
	}

	public boolean onItemSpawned(RuntimeState runtimeState, RuntimeConditionRequirements requirements, ItemSpawned event)
	{
		String itemName = itemNameResolver.resolve(event.getItem().getId());

		if (requirements.getGroundItems().contains(itemName))
		{
			runtimeState.getGroundItems().incrementNearbyGroundItem(itemName);
			return true;
		}

		return false;
	}

	public boolean onItemDespawned(RuntimeState runtimeState, RuntimeConditionRequirements requirements, ItemDespawned event)
	{
		String itemName = itemNameResolver.resolve(event.getItem().getId());

		if (requirements.getGroundItems().contains(itemName))
		{
			runtimeState.getGroundItems().decrementNearbyGroundItem(itemName);
			return true;
		}

		return false;
	}
}
