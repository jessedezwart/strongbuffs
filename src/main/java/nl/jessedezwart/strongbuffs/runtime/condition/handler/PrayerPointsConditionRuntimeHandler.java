package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerPointsCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandlerSupport;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class PrayerPointsConditionRuntimeHandler implements ConditionRuntimeHandler<PrayerPointsCondition>
{
	@Override
	public Class<PrayerPointsCondition> getConditionType()
	{
		return PrayerPointsCondition.class;
	}

	@Override
	public boolean matches(PrayerPointsCondition condition, RuntimeState runtimeState)
	{
		return ConditionRuntimeHandlerSupport.matchesNumeric(condition, runtimeState.getSkills().getPrayerPoints());
	}

	@Override
	public void contributeRequirements(PrayerPointsCondition condition, RuntimeConditionRequirements.Builder builder)
	{
		builder.requirePrayerPoints();
	}

	@Override
	public String formatValue(PrayerPointsCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getSkills().getPrayerPoints() + " prayer";
	}
}
