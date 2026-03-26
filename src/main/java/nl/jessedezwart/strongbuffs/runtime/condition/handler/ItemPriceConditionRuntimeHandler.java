package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.ItemPriceCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.util.ConditionMatcherUtil;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class ItemPriceConditionRuntimeHandler implements ConditionRuntimeAdapter<ItemPriceCondition>
{
	@Override
	public Class<ItemPriceCondition> getConditionType()
	{
		return ItemPriceCondition.class;
	}

	@Override
	public boolean matches(ItemPriceCondition condition, RuntimeState runtimeState)
	{
		return ConditionMatcherUtil.matchesLong(condition,
				runtimeState.getInventory().getItemPrice(condition.getItemName()));
	}

	@Override
	public void contributeRequirements(ItemPriceCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requireItemPrice(condition.getItemName());
	}

	@Override
	public String formatValue(ItemPriceCondition condition, RuntimeState runtimeState)
	{
		return String.valueOf(runtimeState.getInventory().getItemPrice(condition.getItemName()));
	}
}
