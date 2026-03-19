package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.ItemEquippedCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class ItemEquippedConditionRuntimeHandler implements ConditionRuntimeHandler<ItemEquippedCondition>
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
	public void contributeRequirements(ItemEquippedCondition condition, RuntimeConditionRequirements.Builder builder)
	{
		builder.requireEquippedItem(condition.getItemName());
	}

	@Override
	public String formatValue(ItemEquippedCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getInventory().hasEquippedItem(condition.getItemName()) ? "equipped" : "unequipped";
	}
}
