package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.GroundItemCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.impl.InventoryRuntimeState;

public class GroundItemConditionRuntimeHandler implements ConditionRuntimeAdapter<GroundItemCondition>
{
	@Override
	public Class<GroundItemCondition> getConditionType()
	{
		return GroundItemCondition.class;
	}

	@Override
	public boolean matches(GroundItemCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getGroundItems().hasNearbyGroundItem(condition.getItemName());
	}

	@Override
	public void contributeRequirements(GroundItemCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requireGroundItem(condition.getItemName());
	}

	@Override
	public String formatValue(GroundItemCondition condition, RuntimeState runtimeState)
	{
		return String.valueOf(runtimeState.getGroundItems().getNearbyGroundItemCounts()
				.getOrDefault(InventoryRuntimeState.normalizeName(condition.getItemName()), 0));
	}
}
