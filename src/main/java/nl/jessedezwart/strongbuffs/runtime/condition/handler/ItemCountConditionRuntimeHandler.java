package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.ItemCountCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandlerSupport;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class ItemCountConditionRuntimeHandler implements ConditionRuntimeHandler<ItemCountCondition>
{
	@Override
	public Class<ItemCountCondition> getConditionType()
	{
		return ItemCountCondition.class;
	}

	@Override
	public boolean matches(ItemCountCondition condition, RuntimeState runtimeState)
	{
		return ConditionRuntimeHandlerSupport.matchesNumeric(condition,
				runtimeState.getInventory().getInventoryItemCount(condition.getItemName()));
	}

	@Override
	public void contributeRequirements(ItemCountCondition condition, RuntimeConditionRequirements.Builder builder)
	{
		builder.requireInventoryItem(condition.getItemName());
	}

	@Override
	public String formatValue(ItemCountCondition condition, RuntimeState runtimeState)
	{
		return String.valueOf(runtimeState.getInventory().getInventoryItemCount(condition.getItemName()));
	}
}
