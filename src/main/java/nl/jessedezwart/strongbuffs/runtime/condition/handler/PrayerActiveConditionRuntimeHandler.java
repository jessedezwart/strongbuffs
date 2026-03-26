package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerActiveCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class PrayerActiveConditionRuntimeHandler implements ConditionRuntimeAdapter<PrayerActiveCondition>
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
	public void contributeRequirements(PrayerActiveCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requirePrayer(condition.getPrayer());
	}

	@Override
	public String formatValue(PrayerActiveCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getVars().isPrayerActive(condition.getPrayer()) ? "active" : "inactive";
	}
}
