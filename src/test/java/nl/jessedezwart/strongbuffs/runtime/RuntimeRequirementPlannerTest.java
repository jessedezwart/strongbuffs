package nl.jessedezwart.strongbuffs.runtime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeTrackingPlan;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeTrackingPlanner;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeSubscription;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeTrigger;
import org.junit.Test;

public class RuntimeRequirementPlannerTest
{
	private final RuntimeTrackingPlanner runtimeRequirementPlanner = new RuntimeTrackingPlanner();

	@Test
	public void planAlignsSubscriptionsAndTriggersForMixedRequirements()
	{
		RuntimeStateWatchlist requirements = RuntimeStateWatchlist.builder().requireHitpoints()
				.requirePrayer(Prayer.THICK_SKIN).requireXpGainSkill(Skill.ATTACK).requireGroundItem("Rune arrow")
				.requirePlayerInstance().build();

		RuntimeTrackingPlan plan = runtimeRequirementPlanner.plan(requirements);

		assertTrue(plan.requiresSubscription(RuntimeSubscription.STAT_CHANGED));
		assertTrue(plan.requiresSubscription(RuntimeSubscription.VARBIT_CHANGED));
		assertTrue(plan.requiresSubscription(RuntimeSubscription.GAME_TICK));
		assertTrue(plan.requiresSubscription(RuntimeSubscription.GROUND_ITEM));
		assertFalse(plan.requiresSubscription(RuntimeSubscription.ITEM_CONTAINER_CHANGED));

		assertTrue(plan.getTriggers().contains(RuntimeTrigger.HITPOINTS));
		assertTrue(plan.getTriggers().contains(RuntimeTrigger.PRAYER));
		assertTrue(plan.getTriggers().contains(RuntimeTrigger.XP_GAIN));
		assertTrue(plan.getTriggers().contains(RuntimeTrigger.GAME_TICK));
		assertTrue(plan.getTriggers().contains(RuntimeTrigger.GROUND_ITEMS));
		assertTrue(plan.getTriggers().contains(RuntimeTrigger.PLAYER_INSTANCE));
		assertFalse(plan.getTriggers().contains(RuntimeTrigger.INVENTORY));
	}
}
