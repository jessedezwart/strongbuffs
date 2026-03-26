package nl.jessedezwart.strongbuffs;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.ConditionNode;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.panel.state.RulePanelController;
import nl.jessedezwart.strongbuffs.panel.state.RuleValidationResult;

/**
 * Validates persisted rule definitions before they are saved or imported.
 */
@Singleton
public class RuleDefinitionValidator
{
	public RuleValidationResult validate(RuleDefinition ruleDefinition)
	{
		if (ruleDefinition == null)
		{
			return RuleValidationResult.valid();
		}

		Map<String, String> errors = new LinkedHashMap<>();

		if (ruleDefinition.getName() == null || ruleDefinition.getName().trim().isEmpty())
		{
			errors.put(RulePanelController.FIELD_NAME, "Name is required.");
		}

		if (!hasLeafCondition(ruleDefinition.getRootGroup()))
		{
			errors.put(RulePanelController.FIELD_CONDITIONS, "Add at least one condition.");
		}
		else
		{
			String conditionError = validateConditions(ruleDefinition.getRootGroup());

			if (conditionError != null)
			{
				errors.put(RulePanelController.FIELD_CONDITIONS, conditionError);
			}
		}

		if (ruleDefinition.getCooldownTicks() < 0)
		{
			errors.put("activation.cooldownTicks", "Cooldown must be zero or higher.");
		}

		validateAction(errors, ruleDefinition.getAction());
		return RuleValidationResult.of(errors);
	}

	private static String validateConditions(ConditionGroup group)
	{
		for (ConditionNode child : group.getChildren())
		{
			if (child instanceof ConditionGroup)
			{
				String nestedError = validateConditions((ConditionGroup) child);

				if (nestedError != null)
				{
					return nestedError;
				}

				continue;
			}

			Map<String, String> conditionErrors = new LinkedHashMap<>();
			((ConditionDefinition) child).validate(conditionErrors, RulePanelController.FIELD_CONDITIONS);

			if (!conditionErrors.isEmpty())
			{
				return conditionErrors.values().iterator().next();
			}
		}

		return null;
	}

	private static void validateAction(Map<String, String> errors, ActionDefinition actionDefinition)
	{
		if (actionDefinition == null)
		{
			errors.put(RulePanelController.FIELD_ACTION, "Choose an action.");
			return;
		}

		actionDefinition.validate(errors, RulePanelController.FIELD_ACTION);
	}

	private static boolean hasLeafCondition(ConditionGroup group)
	{
		if (group == null)
		{
			return false;
		}

		for (ConditionNode child : group.getChildren())
		{
			if (child instanceof ConditionDefinition)
			{
				return true;
			}

			if (child instanceof ConditionGroup && hasLeafCondition((ConditionGroup) child))
			{
				return true;
			}
		}

		return false;
	}
}
