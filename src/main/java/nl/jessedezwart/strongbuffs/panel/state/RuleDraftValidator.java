package nl.jessedezwart.strongbuffs.panel.state;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;

/**
 * Validates one rule draft before it can be saved.
 *
 * <p>Validation stays in the panel state layer rather than the view so save logic, inline error
 * display, and unsaved-change flows all share the same rules.</p>
 */
@Singleton
public class RuleDraftValidator
{
	/**
	 * Returns field-keyed validation errors that the panel can map back onto concrete widgets.
	 */
	public RuleValidationResult validate(RuleDraft draft)
	{
		if (draft == null)
		{
			return RuleValidationResult.valid();
		}

		Map<String, String> errors = new LinkedHashMap<>();

		if (draft.getName() == null || draft.getName().trim().isEmpty())
		{
			errors.put(RulePanelController.FIELD_NAME, "Name is required.");
		}

		if (!hasLeafCondition(draft.getRootGroup()))
		{
			errors.put(RulePanelController.FIELD_CONDITIONS, "Add at least one condition.");
		}
		else
		{
			String conditionError = validateConditions(draft.getRootGroup());

			if (conditionError != null)
			{
				errors.put(RulePanelController.FIELD_CONDITIONS, conditionError);
			}
		}

		if (draft.getCooldownTicks() < 0)
		{
			errors.put("activation.cooldownTicks", "Cooldown must be zero or higher.");
		}

		validateAction(errors, draft.getAction());
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
