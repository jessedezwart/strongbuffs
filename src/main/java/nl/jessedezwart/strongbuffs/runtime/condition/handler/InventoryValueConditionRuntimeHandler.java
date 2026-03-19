package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.InventoryValueCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.util.ConditionMatcherUtil;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class InventoryValueConditionRuntimeHandler implements ConditionRuntimeAdapter<InventoryValueCondition>
{
	@Override
	public Class<InventoryValueCondition> getConditionType()
	{
		return InventoryValueCondition.class;
	}

	@Override
	public boolean matches(InventoryValueCondition condition, RuntimeState runtimeState)
	{
		return ConditionMatcherUtil.matchesLong(condition,
				runtimeState.getInventory().getInventoryTotalValue());
	}

	@Override
	public void contributeRequirements(InventoryValueCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requireInventoryValue();
	}

	@Override
	public String formatValue(InventoryValueCondition condition, RuntimeState runtimeState)
	{
		return String.valueOf(runtimeState.getInventory().getInventoryTotalValue());
	}
}
