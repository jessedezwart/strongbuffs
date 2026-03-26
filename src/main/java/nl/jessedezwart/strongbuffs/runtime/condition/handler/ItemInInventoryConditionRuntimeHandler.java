package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.ItemInInventoryCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class ItemInInventoryConditionRuntimeHandler implements ConditionRuntimeAdapter<ItemInInventoryCondition>
{
	@Override
	public Class<ItemInInventoryCondition> getConditionType()
	{
		return ItemInInventoryCondition.class;
	}

	@Override
	public boolean matches(ItemInInventoryCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getInventory().hasInventoryItem(condition.getItemName());
	}

	@Override
	public void contributeRequirements(ItemInInventoryCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requireInventoryItem(condition.getItemName());
	}

	@Override
	public String formatValue(ItemInInventoryCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getInventory().hasInventoryItem(condition.getItemName()) ? "present" : "missing";
	}
}
