package nl.jessedezwart.strongbuffs.panel.state;

import java.util.StringJoiner;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.rule.ActivationMode;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.panel.editor.ConditionEditorRegistry;
import nl.jessedezwart.strongbuffs.panel.editor.ActionEditorRegistry;

public final class RuleDescriptions
{
	private RuleDescriptions()
	{
	}

	public static String describeRule(RuleDefinition ruleDefinition, ActionEditorRegistry actionRegistry)
	{
		return describeActivationMode(ruleDefinition.getActivationMode()) + " | " +
			describeAction(ruleDefinition.getAction(), actionRegistry);
	}

	public static String describeActivationMode(ActivationMode activationMode)
	{
		switch (activationMode)
		{
			case ON_ENTER:
				return "On enter";
			case ON_EXIT:
				return "On exit";
			case WHILE_ACTIVE:
			default:
				return "While active";
		}
	}

	public static String describeAction(ActionDefinition actionDefinition, ActionEditorRegistry actionRegistry)
	{
		if (actionDefinition == null)
		{
			return "No action";
		}

		return actionRegistry.describe(actionDefinition);
	}

	public static String describeConditionTree(ConditionGroup group, ConditionEditorRegistry conditionRegistry)
	{
		if (group == null || group.getChildren().isEmpty())
		{
			return "No conditions";
		}

		StringJoiner joiner = new StringJoiner(" " + group.getLogic().name() + " ");

		for (ConditionNode child : group.getChildren())
		{
			if (child instanceof ConditionGroup)
			{
				joiner.add("(" + describeConditionTree((ConditionGroup) child, conditionRegistry) + ")");
				continue;
			}

			joiner.add(conditionRegistry.describe((ConditionDefinition) child));
		}

		return joiner.toString();
	}

	public static String describeComparisonOperator(ComparisonOperator operator)
	{
		switch (operator)
		{
			case GREATER_THAN:
				return ">";
			case GREATER_THAN_OR_EQUAL:
				return ">=";
			case LESS_THAN:
				return "<";
			case LESS_THAN_OR_EQUAL:
			default:
				return "<=";
		}
	}
}
