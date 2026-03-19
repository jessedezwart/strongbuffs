package nl.jessedezwart.strongbuffs.runtime.condition;

import java.util.EnumSet;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeSubscription;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeTrigger;

/**
 * Derives the runtime policy implied by a requirement watchlist.
 *
 * <p>
 * A single plan drives both tracker subscriptions and engine trigger
 * invalidation so event wiring does not drift from reevaluation policy.
 * </p>
 */
@Singleton
public class RuntimeTrackingPlanner
{
	public RuntimeTrackingPlan plan(RuntimeStateWatchlist requirements)
	{
		RuntimeStateWatchlist resolvedRequirements = requirements == null ? RuntimeStateWatchlist.empty()
				: requirements;
		EnumSet<RuntimeSubscription> subscriptions = EnumSet.noneOf(RuntimeSubscription.class);
		EnumSet<RuntimeTrigger> triggers = EnumSet.noneOf(RuntimeTrigger.class);

		if (resolvedRequirements.tracksHitpoints())
		{
			subscriptions.add(RuntimeSubscription.STAT_CHANGED);
			triggers.add(RuntimeTrigger.HITPOINTS);
		}

		if (resolvedRequirements.tracksPrayerPoints())
		{
			subscriptions.add(RuntimeSubscription.STAT_CHANGED);
			triggers.add(RuntimeTrigger.PRAYER_POINTS);
		}

		if (resolvedRequirements.tracksSpecialAttack())
		{
			subscriptions.add(RuntimeSubscription.VARBIT_CHANGED);
			triggers.add(RuntimeTrigger.SPECIAL_ATTACK);
		}

		if (resolvedRequirements.tracksRunEnergy())
		{
			subscriptions.add(RuntimeSubscription.GAME_TICK);
			triggers.add(RuntimeTrigger.RUN_ENERGY);
			triggers.add(RuntimeTrigger.GAME_TICK);
		}

		if (resolvedRequirements.tracksPoison())
		{
			subscriptions.add(RuntimeSubscription.VARBIT_CHANGED);
			triggers.add(RuntimeTrigger.POISON);
		}

		if (resolvedRequirements.tracksSlayerTask())
		{
			subscriptions.add(RuntimeSubscription.VARBIT_CHANGED);
			triggers.add(RuntimeTrigger.SLAYER_TASK);
		}

		if (resolvedRequirements.tracksPlayerLocation())
		{
			subscriptions.add(RuntimeSubscription.GAME_TICK);
			triggers.add(RuntimeTrigger.PLAYER_LOCATION);
			triggers.add(RuntimeTrigger.GAME_TICK);
		}

		if (resolvedRequirements.tracksPlayerInstance())
		{
			subscriptions.add(RuntimeSubscription.GAME_TICK);
			triggers.add(RuntimeTrigger.PLAYER_INSTANCE);
			triggers.add(RuntimeTrigger.GAME_TICK);
		}

		if (!resolvedRequirements.getPrayers().isEmpty())
		{
			subscriptions.add(RuntimeSubscription.VARBIT_CHANGED);
			triggers.add(RuntimeTrigger.PRAYER);
		}

		if (!resolvedRequirements.getRealSkills().isEmpty())
		{
			subscriptions.add(RuntimeSubscription.STAT_CHANGED);
			triggers.add(RuntimeTrigger.REAL_SKILL);
		}

		if (!resolvedRequirements.getXpGainSkills().isEmpty())
		{
			subscriptions.add(RuntimeSubscription.STAT_CHANGED);
			subscriptions.add(RuntimeSubscription.GAME_TICK);
			triggers.add(RuntimeTrigger.XP_GAIN);
			triggers.add(RuntimeTrigger.GAME_TICK);
		}

		if (resolvedRequirements.hasInventoryTracking())
		{
			subscriptions.add(RuntimeSubscription.ITEM_CONTAINER_CHANGED);
			triggers.add(RuntimeTrigger.INVENTORY);
		}

		if (resolvedRequirements.hasEquipmentTracking())
		{
			subscriptions.add(RuntimeSubscription.ITEM_CONTAINER_CHANGED);
			triggers.add(RuntimeTrigger.EQUIPMENT);
		}

		if (resolvedRequirements.hasGroundItemTracking())
		{
			subscriptions.add(RuntimeSubscription.GROUND_ITEM);
			triggers.add(RuntimeTrigger.GROUND_ITEMS);
		}

		return new RuntimeTrackingPlan(resolvedRequirements, subscriptions, triggers);
	}
}
