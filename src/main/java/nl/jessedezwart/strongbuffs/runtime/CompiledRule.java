package nl.jessedezwart.strongbuffs.runtime;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.rule.ActivationMode;

@Getter
public class CompiledRule
{
	private final String id;
	private final String name;
	private final ConditionGroup rootGroup;
	private final ActivationMode activationMode;
	private final int cooldownTicks;
	private final ActionDefinition action;
	private final RuntimeConditionRequirements requirements;
	private final Set<RuntimeTrigger> triggers;

	public CompiledRule(String id, String name, ConditionGroup rootGroup, ActivationMode activationMode, int cooldownTicks,
		ActionDefinition action, RuntimeConditionRequirements requirements, Set<RuntimeTrigger> triggers)
	{
		this.id = id;
		this.name = name;
		this.rootGroup = rootGroup;
		this.activationMode = activationMode == null ? ActivationMode.WHILE_ACTIVE : activationMode;
		this.cooldownTicks = Math.max(0, cooldownTicks);
		this.action = action;
		this.requirements = requirements == null ? RuntimeConditionRequirements.empty() : requirements;
		this.triggers = triggers == null || triggers.isEmpty()
			? Collections.unmodifiableSet(EnumSet.noneOf(RuntimeTrigger.class))
			: Collections.unmodifiableSet(EnumSet.copyOf(triggers));
	}
}
