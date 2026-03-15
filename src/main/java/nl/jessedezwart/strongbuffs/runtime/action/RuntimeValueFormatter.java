package nl.jessedezwart.strongbuffs.runtime.action;

import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeRegistry;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

@Singleton
public class RuntimeValueFormatter
{
	private final ConditionRuntimeRegistry conditionRuntimeRegistry;

	@Inject
	public RuntimeValueFormatter(ConditionRuntimeRegistry conditionRuntimeRegistry)
	{
		this.conditionRuntimeRegistry = conditionRuntimeRegistry;
	}

	public String format(ConditionGroup rootGroup, RuntimeState runtimeState)
	{
		return conditionRuntimeRegistry.formatValue(findPrimaryCondition(rootGroup), runtimeState);
	}

	private ConditionDefinition findPrimaryCondition(ConditionNode node)
	{
		if (node instanceof ConditionDefinition)
		{
			return (ConditionDefinition) node;
		}

		if (node instanceof ConditionGroup)
		{
			for (ConditionNode child : ((ConditionGroup) node).getChildren())
			{
				ConditionDefinition primary = findPrimaryCondition(child);

				if (primary != null)
				{
					return primary;
				}
			}
		}

		return null;
	}
}
