package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import nl.jessedezwart.strongbuffs.model.condition.impl.PlayerInInstanceCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class PlayerInInstanceConditionRuntimeHandler implements ConditionRuntimeAdapter<PlayerInInstanceCondition>
{
	@Override
	public Class<PlayerInInstanceCondition> getConditionType()
	{
		return PlayerInInstanceCondition.class;
	}

	@Override
	public boolean matches(PlayerInInstanceCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getLocation().isInInstance();
	}

	@Override
	public void contributeRequirements(PlayerInInstanceCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requirePlayerInstance();
	}

	@Override
	public String formatValue(PlayerInInstanceCondition condition, RuntimeState runtimeState)
	{
		return runtimeState.getLocation().isInInstance() ? "instance" : "world";
	}
}
