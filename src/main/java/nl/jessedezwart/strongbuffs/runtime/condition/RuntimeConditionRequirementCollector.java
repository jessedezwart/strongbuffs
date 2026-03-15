package nl.jessedezwart.strongbuffs.runtime.condition;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;

@Singleton
public class RuntimeConditionRequirementCollector
{
	private final ConditionRuntimeRegistry conditionRuntimeRegistry;

	@Inject
	public RuntimeConditionRequirementCollector(ConditionRuntimeRegistry conditionRuntimeRegistry)
	{
		this.conditionRuntimeRegistry = conditionRuntimeRegistry;
	}

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
