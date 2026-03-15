package nl.jessedezwart.strongbuffs.runtime.tracker.updater;

import java.util.EnumSet;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeTrigger;

@Singleton
public class LocationStateUpdater
{
	public void refresh(RuntimeState runtimeState, RuntimeConditionRequirements requirements, Client client)
	{
		if (requirements.tracksRunEnergy())
		{
			runtimeState.getLocation().setRunEnergyPercent(client.getEnergy() / 100);
		}

		if (requirements.tracksPlayerLocation())
		{
			runtimeState.getLocation().setPlayerLocation(readPlayerLocation(client));
		}

		if (requirements.tracksPlayerInstance())
		{
			runtimeState.getLocation().setInInstance(client.getTopLevelWorldView().isInstance());
		}
	}

	public EnumSet<RuntimeTrigger> onGameTick(RuntimeState runtimeState, RuntimeConditionRequirements requirements,
		Client client)
	{
		EnumSet<RuntimeTrigger> triggers = EnumSet.of(RuntimeTrigger.GAME_TICK);

		if (requirements.tracksRunEnergy())
		{
			runtimeState.getLocation().setRunEnergyPercent(client.getEnergy() / 100);
			triggers.add(RuntimeTrigger.RUN_ENERGY);
		}

		if (requirements.tracksPlayerLocation())
		{
			runtimeState.getLocation().setPlayerLocation(readPlayerLocation(client));
			triggers.add(RuntimeTrigger.PLAYER_LOCATION);
		}

		if (requirements.tracksPlayerInstance())
		{
			runtimeState.getLocation().setInInstance(client.getTopLevelWorldView().isInstance());
			triggers.add(RuntimeTrigger.PLAYER_INSTANCE);
		}

		if (!requirements.getXpGainSkills().isEmpty())
		{
			triggers.add(RuntimeTrigger.XP_GAIN);
		}

		return triggers;
	}

	private static WorldPoint readPlayerLocation(Client client)
	{
		return client.getLocalPlayer() == null ? null : client.getLocalPlayer().getWorldLocation();
	}
}
