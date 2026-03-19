package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.RunEnergyCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandlerSupport;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class RunEnergyConditionRuntimeHandler implements ConditionRuntimeHandler<RunEnergyCondition>
{
	@Override
	public Class<RunEnergyCondition> getConditionType()
	{
		return RunEnergyCondition.class;
	}

	@Override
	public boolean matches(RunEnergyCondition condition, RuntimeState runtimeState)
	{
		return ConditionRuntimeHandlerSupport.matchesNumeric(condition,
				runtimeState.getLocation().getRunEnergyPercent());
	}

	@Override
	public void contributeRequirements(RunEnergyCondition condition, RuntimeConditionRequirements.Builder builder)
	{
		builder.requireRunEnergy();
	}

	@Override
	public String formatValue(RunEnergyCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getLocation().getRunEnergyPercent() + "%";
	}
}
