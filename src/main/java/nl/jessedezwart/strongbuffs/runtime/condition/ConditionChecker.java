package nl.jessedezwart.strongbuffs.runtime.condition;

import javax.inject.Inject;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionLogic;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

/**
 * Evaluates persisted condition definitions against cached runtime state.
 */
public class ConditionChecker
{
	private final ConditionRuntimeRegistry conditionRuntimeRegistry;

	public ConditionChecker()
	{
		this(new ConditionRuntimeRegistry());
	}

	@Inject
	public ConditionChecker(ConditionRuntimeRegistry conditionRuntimeRegistry)
	{
		this.conditionRuntimeRegistry = conditionRuntimeRegistry;
	}

	public boolean evaluate(ConditionGroup group, RuntimeState state)
	{
		if (group == null || state == null || group.getChildren().isEmpty())
		{
			return false;
		}

		if (group.getLogic() == ConditionLogic.OR)
		{
			for (ConditionNode child : group.getChildren())
			{
				if (evaluateNode(child, state))
				{
					return true;
				}
			}

			return false;
		}

		for (ConditionNode child : group.getChildren())
		{
			if (!evaluateNode(child, state))
			{
				return false;
			}
		}

		return true;
	}

	public boolean evaluate(ConditionDefinition condition, RuntimeState state)
	{
		if (condition == null || state == null)
		{
			return false;
		}

		return conditionRuntimeRegistry.matches(condition, state);
	}

	private boolean evaluateNode(ConditionNode node, RuntimeState state)
	{
		if (node instanceof ConditionGroup)
		{
			return evaluate((ConditionGroup) node, state);
		}

		if (node instanceof ConditionDefinition)
		{
			return evaluate((ConditionDefinition) node, state);
		}

		return false;
	}
}
