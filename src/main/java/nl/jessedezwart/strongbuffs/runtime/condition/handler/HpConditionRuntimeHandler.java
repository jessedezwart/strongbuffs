package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandlerSupport;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class HpConditionRuntimeHandler implements ConditionRuntimeHandler<HpCondition>
{
	@Override
	public Class<HpCondition> getConditionType()
	{
		return HpCondition.class;
	}

	@Override
	public boolean matches(HpCondition condition, RuntimeState runtimeState)
	{
		return ConditionRuntimeHandlerSupport.matchesNumeric(condition, runtimeState.getSkills().getHitpoints());
	}

	@Override
	public void contributeRequirements(HpCondition condition, RuntimeConditionRequirements.Builder builder)
	{
		builder.requireHitpoints();
	}

	@Override
	public String formatValue(HpCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getSkills().getHitpoints() + " hp";
	}
}
