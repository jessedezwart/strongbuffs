package nl.jessedezwart.strongbuffs.runtime.engine;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.rule.ActivationMode;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeTrigger;

/**
 * Immutable runtime representation of one persisted rule.
 *
 * <p>
 * Compiled rules normalize defaults and carry the precomputed requirement and
 * trigger metadata the engine needs for fast incremental evaluation.
 * </p>
 */
@Getter
public class CompiledRule
{
	private final String id;
	private final String name;
	private final ConditionGroup rootGroup;
	private final ActivationMode activationMode;
	private final int cooldownTicks;
	private final ActionDefinition action;
	private final RuntimeStateWatchlist requirements;
	private final Set<RuntimeTrigger> triggers;

	public CompiledRule(String id, String name, ConditionGroup rootGroup, ActivationMode activationMode,
			int cooldownTicks, ActionDefinition action, RuntimeStateWatchlist requirements,
			Set<RuntimeTrigger> triggers)
	{
		this.id = id;
		this.name = name;
		this.rootGroup = rootGroup;
		this.activationMode = activationMode == null ? ActivationMode.WHILE_ACTIVE : activationMode;
		this.cooldownTicks = Math.max(0, cooldownTicks);
		this.action = action;
		this.requirements = requirements == null ? RuntimeStateWatchlist.empty() : requirements;
		this.triggers = triggers == null || triggers.isEmpty()
				? Collections.unmodifiableSet(EnumSet.noneOf(RuntimeTrigger.class))
				: Collections.unmodifiableSet(EnumSet.copyOf(triggers));
	}
}
