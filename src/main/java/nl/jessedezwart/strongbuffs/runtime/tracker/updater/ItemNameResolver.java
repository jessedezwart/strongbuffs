package nl.jessedezwart.strongbuffs.runtime.tracker.updater;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.game.ItemManager;
import nl.jessedezwart.strongbuffs.runtime.state.InventoryRuntimeState;

@Singleton
public class ItemNameResolver
{
	private final ItemManager itemManager;

	@Inject
	public ItemNameResolver(ItemManager itemManager)
	{
		this.itemManager = itemManager;
	}

	public String resolve(int itemId)
	{
		int canonicalId = itemManager.canonicalize(itemId);
		return InventoryRuntimeState.normalizeName(itemManager.getItemComposition(canonicalId).getName());
	}
}
