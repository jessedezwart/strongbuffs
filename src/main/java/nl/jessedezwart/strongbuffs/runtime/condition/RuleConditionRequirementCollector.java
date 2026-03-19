package nl.jessedezwart.strongbuffs.runtime.condition;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.ConditionNode;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;

/**
 * Walks persisted rule trees and aggregates the runtime state slices they
 * require.
 */
@Singleton
public class RuleConditionRequirementCollector
{
	private final ConditionRuntimeAdapterRegistry conditionRuntimeRegistry;

	@Inject
	public RuleConditionRequirementCollector(ConditionRuntimeAdapterRegistry conditionRuntimeRegistry)
	{
		this.conditionRuntimeRegistry = conditionRuntimeRegistry;
	}

	/**
	 * Collects the union of requirements for all enabled rules.
	 */
	public RuntimeStateWatchlist fromRules(List<RuleDefinition> rules)
	{
		RuntimeStateWatchlist.Builder builder = RuntimeStateWatchlist.builder();

		if (rules == null || rules.isEmpty())
		{
			return builder.build();
		}

		for (RuleDefinition rule : rules)
		{
			if (rule == null || !rule.isEnabled() || rule.getRootGroup() == null)
			{
				continue;
			}

			collect(rule.getRootGroup(), builder);
		}

		return builder.build();
	}

	/**
	 * Recursively contributes requirements from one condition tree branch.
	 */
	public void collect(ConditionGroup group, RuntimeStateWatchlist.Builder builder)
	{
		if (group == null)
		{
			return;
		}

		for (ConditionNode child : group.getChildren())
		{
			if (child instanceof ConditionGroup)
			{
				collect((ConditionGroup) child, builder);
			}
			else if (child instanceof ConditionDefinition)
			{
				conditionRuntimeRegistry.contributeRequirements((ConditionDefinition) child, builder);
			}
		}
	}
}
