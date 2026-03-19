package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.SlayerTaskCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.utils.ConditionMatcherUtils;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class SlayerTaskConditionRuntimeHandler implements ConditionRuntimeAdapter<SlayerTaskCondition>
{
	@Override
	public Class<SlayerTaskCondition> getConditionType()
	{
		return SlayerTaskCondition.class;
	}

	@Override
	public boolean matches(SlayerTaskCondition condition, RuntimeState runtimeState)
	{
		if (condition.getCheck() == null)
		{
			return false;
		}

		if (condition.getCheck() == SlayerTaskCondition.SlayerTaskCheck.TASK_ACTIVE)
		{
			return runtimeState.getVars().isSlayerTaskActive();
		}

		return ConditionMatcherUtils.matches(condition, runtimeState.getVars().getSlayerTaskRemaining());
	}

	@Override
	public void contributeRequirements(SlayerTaskCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requireSlayerTask();
	}

	@Override
	public String formatValue(SlayerTaskCondition condition, RuntimeState runtimeState)
	{
		if (condition.getCheck() == SlayerTaskCondition.SlayerTaskCheck.KILLS_REMAINING)
		{
			return runtimeState.getVars().getSlayerTaskRemaining() + " left";
		}

		return runtimeState.getVars().isSlayerTaskActive() ? "active" : "inactive";
	}
}
