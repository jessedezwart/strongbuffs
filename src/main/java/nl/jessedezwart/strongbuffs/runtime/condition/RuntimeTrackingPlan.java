package nl.jessedezwart.strongbuffs.runtime.condition;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeSubscription;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeTrigger;

/**
 * Immutable runtime policy derived from a requirement watchlist.
 *
 * <p>
 * This is the single source of truth for both RuneLite event subscriptions and
 * rule reevaluation triggers so the tracker and engine stay aligned as new
 * requirement types are added.
 * </p>
 */
@Getter
public final class RuntimeTrackingPlan
{
	private static final RuntimeTrackingPlan EMPTY = new RuntimeTrackingPlan(RuntimeStateWatchlist.empty(),
			EnumSet.noneOf(RuntimeSubscription.class), EnumSet.noneOf(RuntimeTrigger.class));

	private final RuntimeStateWatchlist requirements;
	private final Set<RuntimeSubscription> subscriptions;
	private final Set<RuntimeTrigger> triggers;

	public RuntimeTrackingPlan(RuntimeStateWatchlist requirements, Set<RuntimeSubscription> subscriptions,
			Set<RuntimeTrigger> triggers)
	{
		this.requirements = requirements == null ? RuntimeStateWatchlist.empty() : requirements;
		this.subscriptions = subscriptions == null || subscriptions.isEmpty()
				? Collections.unmodifiableSet(EnumSet.noneOf(RuntimeSubscription.class))
				: Collections.unmodifiableSet(EnumSet.copyOf(subscriptions));
		this.triggers = triggers == null || triggers.isEmpty()
				? Collections.unmodifiableSet(EnumSet.noneOf(RuntimeTrigger.class))
				: Collections.unmodifiableSet(EnumSet.copyOf(triggers));
	}

	public static RuntimeTrackingPlan empty()
	{
		return EMPTY;
	}

	public boolean requiresSubscription(RuntimeSubscription subscription)
	{
		return subscription != null && subscriptions.contains(subscription);
	}
}
