package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerPointsCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.util.ConditionMatcherUtil;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class PrayerPointsConditionRuntimeHandler implements ConditionRuntimeAdapter<PrayerPointsCondition>
{
	@Override
	public Class<PrayerPointsCondition> getConditionType()
	{
		return PrayerPointsCondition.class;
	}

	@Override
	public boolean matches(PrayerPointsCondition condition, RuntimeState runtimeState)
	{
		return ConditionMatcherUtil.matches(condition, runtimeState.getSkills().getPrayerPoints());
	}

	@Override
	public void contributeRequirements(PrayerPointsCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requirePrayerPoints();
	}

	@Override
	public String formatValue(PrayerPointsCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getSkills().getPrayerPoints() + " prayer";
	}
}
