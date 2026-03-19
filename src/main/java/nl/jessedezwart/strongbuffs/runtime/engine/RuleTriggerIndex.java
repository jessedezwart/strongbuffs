package nl.jessedezwart.strongbuffs.runtime.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jessedezwart.strongbuffs.runtime.tracker.RuntimeTrigger;

/**
 * Index from runtime triggers to the compiled rules affected by those triggers.
 *
 * <p>This is the key optimization that lets the engine reevaluate only impacted rules on ordinary
 * events while still falling back to full passes for refresh and clear boundaries.</p>
 */
public class RuleTriggerIndex
{
	private static final RuleTriggerIndex EMPTY = new RuleTriggerIndex(Collections.emptyList(),
		new EnumMap<>(RuntimeTrigger.class));

	private final List<CompiledRule> allRules;
	private final Map<RuntimeTrigger, List<CompiledRule>> rulesByTrigger;

	private RuleTriggerIndex(List<CompiledRule> allRules, Map<RuntimeTrigger, List<CompiledRule>> rulesByTrigger)
	{
		this.allRules = Collections.unmodifiableList(new ArrayList<>(allRules));
		this.rulesByTrigger = new EnumMap<>(RuntimeTrigger.class);

		for (Map.Entry<RuntimeTrigger, List<CompiledRule>> entry : rulesByTrigger.entrySet())
		{
			this.rulesByTrigger.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
		}
	}

	public static RuleTriggerIndex empty()
	{
		return EMPTY;
	}

	/**
	 * Builds an index from each trigger to the rules that depend on it.
	 */
	public static RuleTriggerIndex fromRules(List<CompiledRule> rules)
	{
		Map<RuntimeTrigger, List<CompiledRule>> indexed = new EnumMap<>(RuntimeTrigger.class);

		for (CompiledRule rule : rules)
		{
			for (RuntimeTrigger trigger : rule.getTriggers())
			{
				indexed.computeIfAbsent(trigger, ignored -> new ArrayList<>()).add(rule);
			}
		}

		return new RuleTriggerIndex(rules, indexed);
	}

	/**
	 * Returns the unique compiled rules affected by the provided triggers.
	 */
	public Collection<CompiledRule> getRulesForTriggers(Set<RuntimeTrigger> triggers)
	{
		if (triggers == null || triggers.isEmpty())
		{
			return Collections.emptyList();
		}

		if (triggers.contains(RuntimeTrigger.FULL_REFRESH) || triggers.contains(RuntimeTrigger.CLEAR))
		{
			return allRules;
		}

		LinkedHashSet<CompiledRule> matchedRules = new LinkedHashSet<>();

		for (RuntimeTrigger trigger : triggers)
		{
			List<CompiledRule> rules = rulesByTrigger.get(trigger);

			if (rules != null)
			{
				matchedRules.addAll(rules);
			}
		}

		return matchedRules;
	}
}
