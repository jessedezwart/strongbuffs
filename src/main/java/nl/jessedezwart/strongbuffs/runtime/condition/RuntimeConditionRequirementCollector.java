package nl.jessedezwart.strongbuffs.runtime.condition;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.ConditionNode;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;

/**
 * Walks persisted rule trees and aggregates the runtime state slices they require.
 *
 * <p>This allows the tracker to subscribe and refresh selectively instead of maintaining every
 * possible slice of game state all the time.</p>
 */
@Singleton
public class RuntimeConditionRequirementCollector
{
	private final ConditionRuntimeRegistry conditionRuntimeRegistry;

	@Inject
	public RuntimeConditionRequirementCollector(ConditionRuntimeRegistry conditionRuntimeRegistry)
	{
		this.conditionRuntimeRegistry = conditionRuntimeRegistry;
	}

	/**
	 * Collects the union of requirements for all enabled rules.
	 */
	public RuntimeConditionRequirements fromRules(List<RuleDefinition> rules)
	{
		RuntimeConditionRequirements.Builder builder = RuntimeConditionRequirements.builder();

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
	public void collect(ConditionGroup group, RuntimeConditionRequirements.Builder builder)
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
