package nl.jessedezwart.strongbuffs;

import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.ConditionNode;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;

/**
 * Deep-copy helpers for persisted rule definitions.
 */
public final class RuleDefinitionCloner
{
	private RuleDefinitionCloner()
	{
	}

	public static RuleDefinition copyRule(RuleDefinition sourceRule)
	{
		if (sourceRule == null)
		{
			return null;
		}

		RuleDefinition copy = new RuleDefinition();
		copy.setSchemaVersion(sourceRule.getSchemaVersion());
		copy.setId(sourceRule.getId());
		copy.setName(sourceRule.getName());
		copy.setEnabled(sourceRule.isEnabled());
		copy.setRootGroup(copyGroup(sourceRule.getRootGroup()));
		copy.setActivationMode(sourceRule.getActivationMode());
		copy.setCooldownTicks(sourceRule.getCooldownTicks());
		copy.setAction(copyAction(sourceRule.getAction()));
		return copy;
	}

	public static ConditionGroup copyGroup(ConditionGroup source)
	{
		ConditionGroup copy = new ConditionGroup();
		copy.setLogic(source.getLogic());

		for (ConditionNode child : source.getChildren())
		{
			if (child instanceof ConditionGroup)
			{
				copy.getChildren().add(copyGroup((ConditionGroup) child));
				continue;
			}

			copy.getChildren().add(((ConditionDefinition) child).copy());
		}

		return copy;
	}

	public static ActionDefinition copyAction(ActionDefinition actionDefinition)
	{
		return actionDefinition == null ? null : actionDefinition.copy();
	}
}
