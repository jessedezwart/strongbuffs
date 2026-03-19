package nl.jessedezwart.strongbuffs.runtime.condition.handler;

import net.runelite.api.coords.WorldPoint;
import nl.jessedezwart.strongbuffs.model.condition.impl.PlayerInZoneCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public class PlayerInZoneConditionRuntimeHandler implements ConditionRuntimeAdapter<PlayerInZoneCondition>
{
	@Override
	public Class<PlayerInZoneCondition> getConditionType()
	{
		return PlayerInZoneCondition.class;
	}

	@Override
	public boolean matches(PlayerInZoneCondition condition, RuntimeState runtimeState)
	{
		WorldPoint playerLocation = runtimeState.getLocation().getPlayerLocation();

		if (playerLocation == null)
		{
			return false;
		}

		return playerLocation.getPlane() == condition.getPlane() && playerLocation.getX() >= condition.getSouthWestX()
				&& playerLocation.getX() <= condition.getNorthEastX()
				&& playerLocation.getY() >= condition.getSouthWestY()
				&& playerLocation.getY() <= condition.getNorthEastY();
	}

	@Override
	public void contributeRequirements(PlayerInZoneCondition condition, RuntimeStateWatchlist.Builder builder)
	{
		builder.requirePlayerLocation();
	}

	@Override
	public String formatValue(PlayerInZoneCondition condition, RuntimeState runtimeState)
	{
		return matches(condition, runtimeState) ? "inside" : "outside";
	}
}
