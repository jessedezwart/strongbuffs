package nl.jessedezwart.strongbuffs.runtime.engine;

import java.util.EnumSet;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeConditionRequirements;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeTrigger;

@Singleton
public class RuntimeTriggerPlanner
{
	public EnumSet<RuntimeTrigger> plan(RuntimeConditionRequirements requirements)
	{
		EnumSet<RuntimeTrigger> triggers = EnumSet.noneOf(RuntimeTrigger.class);

		if (requirements == null)
		{
			return triggers;
		}

		if (requirements.tracksHitpoints())
		{
			triggers.add(RuntimeTrigger.HITPOINTS);
		}

		if (requirements.tracksPrayerPoints())
		{
			triggers.add(RuntimeTrigger.PRAYER_POINTS);
		}

		if (requirements.tracksSpecialAttack())
		{
			triggers.add(RuntimeTrigger.SPECIAL_ATTACK);
		}

		if (requirements.tracksRunEnergy())
		{
			triggers.add(RuntimeTrigger.RUN_ENERGY);
			triggers.add(RuntimeTrigger.GAME_TICK);
		}

		if (requirements.tracksPoison())
		{
			triggers.add(RuntimeTrigger.POISON);
		}

		if (requirements.tracksSlayerTask())
		{
			triggers.add(RuntimeTrigger.SLAYER_TASK);
		}

		if (requirements.tracksPlayerLocation())
		{
			triggers.add(RuntimeTrigger.PLAYER_LOCATION);
			triggers.add(RuntimeTrigger.GAME_TICK);
		}

		if (requirements.tracksPlayerInstance())
		{
			triggers.add(RuntimeTrigger.PLAYER_INSTANCE);
			triggers.add(RuntimeTrigger.GAME_TICK);
		}

		if (!requirements.getPrayers().isEmpty())
		{
			triggers.add(RuntimeTrigger.PRAYER);
		}

		if (!requirements.getRealSkills().isEmpty())
		{
			triggers.add(RuntimeTrigger.REAL_SKILL);
		}

		if (!requirements.getXpGainSkills().isEmpty())
		{
			triggers.add(RuntimeTrigger.XP_GAIN);
			triggers.add(RuntimeTrigger.GAME_TICK);
		}

		if (requirements.hasInventoryTracking())
		{
			triggers.add(RuntimeTrigger.INVENTORY);
		}

		if (requirements.hasEquipmentTracking())
		{
			triggers.add(RuntimeTrigger.EQUIPMENT);
		}

		if (requirements.hasGroundItemTracking())
		{
			triggers.add(RuntimeTrigger.GROUND_ITEMS);
		}

		return triggers;
	}
}
