package nl.jessedezwart.strongbuffs.runtime.tracker.updater;

import java.util.EnumSet;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.impl.VarRuntimeState;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeTrigger;

@Singleton
public class VarStateUpdater
{
	public void refresh(RuntimeState runtimeState, RuntimeStateWatchlist requirements, Client client)
	{
		if (requirements.tracksSpecialAttack())
		{
			runtimeState.getVars().setSpecialAttackPercent(client.getVarpValue(VarPlayerID.SA_ENERGY) / 10);
		}

		if (requirements.tracksPoison())
		{
			runtimeState.getVars().setPoisonState(readPoisonState(client));
		}

		if (requirements.tracksSlayerTask())
		{
			int remaining = client.getVarpValue(VarPlayerID.SLAYER_COUNT);
			runtimeState.getVars().setSlayerTaskRemaining(remaining);
			runtimeState.getVars().setSlayerTaskActive(remaining > 0);
		}

		for (Prayer prayer : requirements.getPrayers())
		{
			runtimeState.getVars().setPrayerActive(prayer, isPrayerActive(client, prayer));
		}
	}

	public EnumSet<RuntimeTrigger> onVarbitChanged(RuntimeState runtimeState, RuntimeStateWatchlist requirements,
			Client client, VarbitChanged event)
	{
		EnumSet<RuntimeTrigger> triggers = EnumSet.noneOf(RuntimeTrigger.class);

		if (requirements.tracksSpecialAttack() && event.getVarpId() == VarPlayerID.SA_ENERGY)
		{
			runtimeState.getVars().setSpecialAttackPercent(client.getVarpValue(VarPlayerID.SA_ENERGY) / 10);
			triggers.add(RuntimeTrigger.SPECIAL_ATTACK);
		}

		if (requirements.tracksPoison() && event.getVarpId() == VarPlayerID.POISON)
		{
			runtimeState.getVars().setPoisonState(readPoisonState(client));
			triggers.add(RuntimeTrigger.POISON);
		}

		if (requirements.tracksSlayerTask() && event.getVarpId() == VarPlayerID.SLAYER_COUNT)
		{
			int remaining = client.getVarpValue(VarPlayerID.SLAYER_COUNT);
			runtimeState.getVars().setSlayerTaskRemaining(remaining);
			runtimeState.getVars().setSlayerTaskActive(remaining > 0);
			triggers.add(RuntimeTrigger.SLAYER_TASK);
		}

		if (!requirements.getPrayers().isEmpty() && event.getVarbitId() != -1)
		{
			for (Prayer prayer : requirements.getPrayers())
			{
				if (prayer.getVarbit() == event.getVarbitId())
				{
					runtimeState.getVars().setPrayerActive(prayer, isPrayerActive(client, prayer));
					triggers.add(RuntimeTrigger.PRAYER);
					break;
				}
			}
		}

		return triggers;
	}

	private static VarRuntimeState.PoisonState readPoisonState(Client client)
	{
		int poisonValue = client.getVarpValue(VarPlayerID.POISON);

		if (poisonValue >= 1000000)
		{
			return VarRuntimeState.PoisonState.VENOM;
		}

		if (poisonValue > 0)
		{
			return VarRuntimeState.PoisonState.POISON;
		}

		return VarRuntimeState.PoisonState.NONE;
	}

	private static boolean isPrayerActive(Client client, Prayer prayer)
	{
		return client.getVarbitValue(prayer.getVarbit()) > 0;
	}
}
