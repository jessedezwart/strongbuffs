package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.BankValueCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.util.ConditionMatcherUtil;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class BankValueConditionRuntimeHandler implements ConditionRuntimeAdapter<BankValueCondition>
{
	@Override
	public Class<BankValueCondition> getConditionType()
	{
		return BankValueCondition.class;
	}

	@Override
	public boolean matches(BankValueCondition condition, RuntimeState runtimeState)
	{
		return ConditionMatcherUtil.matchesLong(condition,
				runtimeState.getInventory().getBankTotalValue());
	}

	@Override
	public void contributeRequirements(BankValueCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requireBankValue();
	}

	@Override
	public String formatValue(BankValueCondition condition, RuntimeState runtimeState)
	{
		return String.valueOf(runtimeState.getInventory().getBankTotalValue());
	}
}
