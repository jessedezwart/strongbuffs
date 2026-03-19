package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.SpecialAttackCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandlerSupport;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class SpecialAttackConditionRuntimeHandler implements ConditionRuntimeHandler<SpecialAttackCondition>
{
	@Override
	public Class<SpecialAttackCondition> getConditionType()
	{
		return SpecialAttackCondition.class;
	}

	@Override
	public boolean matches(SpecialAttackCondition condition, RuntimeState runtimeState)
	{
		return ConditionRuntimeHandlerSupport.matchesNumeric(condition,
				runtimeState.getVars().getSpecialAttackPercent());
	}

	@Override
	public void contributeRequirements(SpecialAttackCondition condition, RuntimeConditionRequirements.Builder builder)
	{
		builder.requireSpecialAttack();
	}

	@Override
	public String formatValue(SpecialAttackCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getVars().getSpecialAttackPercent() + "%";
	}
}
