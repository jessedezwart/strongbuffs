package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.util.ConditionMatcherUtil;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class HpConditionRuntimeHandler implements ConditionRuntimeAdapter<HpCondition>
{
	@Override
	public Class<HpCondition> getConditionType()
	{
		return HpCondition.class;
	}

	@Override
	public boolean matches(HpCondition condition, RuntimeState runtimeState)
	{
		return ConditionMatcherUtil.matches(condition, runtimeState.getSkills().getHitpoints());
	}

	@Override
	public void contributeRequirements(HpCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requireHitpoints();
	}

	@Override
	public String formatValue(HpCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getSkills().getHitpoints() + " hp";
	}
}
