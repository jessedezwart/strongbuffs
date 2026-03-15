package nl.jessedezwart.strongbuffs.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class CompiledRuleSet
{
	private static final CompiledRuleSet EMPTY = new CompiledRuleSet(Collections.emptyList(),
		RuntimeConditionRequirements.empty(), RuleTriggerIndex.empty());

	private final List<CompiledRule> rules;
	private final RuntimeConditionRequirements requirements;
	private final RuleTriggerIndex triggerIndex;

	public CompiledRuleSet(List<CompiledRule> rules, RuntimeConditionRequirements requirements, RuleTriggerIndex triggerIndex)
	{
		this.rules = Collections.unmodifiableList(new ArrayList<>(rules));
		this.requirements = requirements == null ? RuntimeConditionRequirements.empty() : requirements;
		this.triggerIndex = triggerIndex == null ? RuleTriggerIndex.empty() : triggerIndex;
	}

	public static CompiledRuleSet empty()
	{
		return EMPTY;
	}
}
