package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerActiveCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeHandler;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class PrayerActiveConditionRuntimeHandler implements ConditionRuntimeHandler<PrayerActiveCondition>
{
	@Override
	public Class<PrayerActiveCondition> getConditionType()
	{
		return PrayerActiveCondition.class;
	}

	@Override
	public boolean matches(PrayerActiveCondition condition, RuntimeState runtimeState)
	{
		return condition.getActive() == runtimeState.getVars().isPrayerActive(condition.getPrayer());
	}

	@Override
	public void contributeRequirements(PrayerActiveCondition condition, RuntimeConditionRequirements.Builder builder)
	{
		builder.requirePrayer(condition.getPrayer());
	}

	@Override
	public String formatValue(PrayerActiveCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getVars().isPrayerActive(condition.getPrayer()) ? "active" : "inactive";
	}
}
