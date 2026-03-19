package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.ItemCountCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.utils.ConditionMatcherUtils;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class ItemCountConditionRuntimeHandler implements ConditionRuntimeAdapter<ItemCountCondition>
{
	@Override
	public Class<ItemCountCondition> getConditionType()
	{
		return ItemCountCondition.class;
	}

	@Override
	public boolean matches(ItemCountCondition condition, RuntimeState runtimeState)
	{
		return ConditionMatcherUtils.matches(condition,
				runtimeState.getInventory().getInventoryItemCount(condition.getItemName()));
	}

	@Override
	public void contributeRequirements(ItemCountCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requireInventoryItem(condition.getItemName());
	}

	@Override
	public String formatValue(ItemCountCondition condition, RuntimeState runtimeState)
	{
		return String.valueOf(runtimeState.getInventory().getInventoryItemCount(condition.getItemName()));
	}
}
