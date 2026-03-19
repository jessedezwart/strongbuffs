package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.ItemEquippedCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class ItemEquippedConditionRuntimeHandler implements ConditionRuntimeAdapter<ItemEquippedCondition>
{
	@Override
	public Class<ItemEquippedCondition> getConditionType()
	{
		return ItemEquippedCondition.class;
	}

	@Override
	public boolean matches(ItemEquippedCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getInventory().hasEquippedItem(condition.getItemName());
	}

	@Override
	public void contributeRequirements(ItemEquippedCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requireEquippedItem(condition.getItemName());
	}

	@Override
	public String formatValue(ItemEquippedCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getInventory().hasEquippedItem(condition.getItemName()) ? "equipped" : "unequipped";
	}
}
