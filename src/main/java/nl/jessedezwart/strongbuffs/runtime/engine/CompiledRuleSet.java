package nl.jessedezwart.strongbuffs.runtime.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeStateWatchlist;
import nl.jessedezwart.strongbuffs.runtime.condition.RuntimeTrackingPlan;

/**
 * Immutable bundle of compiled rules plus their aggregate runtime metadata.
 */
@Getter
public class CompiledRuleSet
{
	private static final CompiledRuleSet EMPTY = new CompiledRuleSet(Collections.emptyList(),
			RuntimeTrackingPlan.empty(), RuleTriggerIndex.empty());

	private final List<CompiledRule> rules;
	private final RuntimeStateWatchlist requirements;
	private final RuntimeTrackingPlan requirementPlan;
	private final RuleTriggerIndex triggerIndex;

	public CompiledRuleSet(List<CompiledRule> rules, RuntimeTrackingPlan requirementPlan, RuleTriggerIndex triggerIndex)
	{
		this.rules = Collections.unmodifiableList(new ArrayList<>(rules));
		this.requirementPlan = requirementPlan == null ? RuntimeTrackingPlan.empty() : requirementPlan;
		this.requirements = this.requirementPlan.getRequirements();
		this.triggerIndex = triggerIndex == null ? RuleTriggerIndex.empty() : triggerIndex;
	}

	public static CompiledRuleSet empty()
	{
		return EMPTY;
	}
}
