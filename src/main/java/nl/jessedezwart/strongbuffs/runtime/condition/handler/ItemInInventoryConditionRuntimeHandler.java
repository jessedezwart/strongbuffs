package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.ItemInInventoryCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class ItemInInventoryConditionRuntimeHandler implements ConditionRuntimeHandler<ItemInInventoryCondition>
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
	public void contributeRequirements(ItemInInventoryCondition condition, RuntimeConditionRequirements.Builder builder)
	{
		builder.requireInventoryItem(condition.getItemName());
	}

	@Override
	public String formatValue(ItemInInventoryCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getInventory().hasInventoryItem(condition.getItemName()) ? "present" : "missing";
	}
}
