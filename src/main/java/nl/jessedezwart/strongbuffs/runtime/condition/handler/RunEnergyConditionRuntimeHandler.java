package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.RunEnergyCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.util.ConditionMatcherUtil;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class RunEnergyConditionRuntimeHandler implements ConditionRuntimeAdapter<RunEnergyCondition>
{
	@Override
	public Class<RunEnergyCondition> getConditionType()
	{
		return RunEnergyCondition.class;
	}

	@Override
	public boolean matches(RunEnergyCondition condition, RuntimeState runtimeState)
	{
		return ConditionMatcherUtil.matches(condition, runtimeState.getLocation().getRunEnergyPercent());
	}

	@Override
	public void contributeRequirements(RunEnergyCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requireRunEnergy();
	}

	@Override
	public String formatValue(RunEnergyCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getLocation().getRunEnergyPercent() + "%";
	}
}
