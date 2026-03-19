package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.SpecialAttackCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.utils.ConditionMatcherUtils;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class SpecialAttackConditionRuntimeHandler implements ConditionRuntimeAdapter<SpecialAttackCondition>
{
	@Override
	public Class<SpecialAttackCondition> getConditionType()
	{
		return SpecialAttackCondition.class;
	}

	@Override
	public boolean matches(SpecialAttackCondition condition, RuntimeState runtimeState)
	{
		return ConditionMatcherUtils.matches(condition, runtimeState.getVars().getSpecialAttackPercent());
	}

	@Override
	public void contributeRequirements(SpecialAttackCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requireSpecialAttack();
	}

	@Override
	public String formatValue(SpecialAttackCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getVars().getSpecialAttackPercent() + "%";
	}
}
