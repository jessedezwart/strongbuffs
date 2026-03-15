package nl.jessedezwart.strongbuffs.panel.state;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.rule.ActivationMode;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;

/**
 * UI-only mutable copy of a rule definition used to isolate Swing edits from
 * persisted state.
 */
@Data
@NoArgsConstructor
public class RuleDraft
{
	private String id = UUID.randomUUID().toString();
	private String name = "";
	private boolean enabled = true;
	private ConditionGroup rootGroup = new ConditionGroup();
	private ActivationMode activationMode = ActivationMode.WHILE_ACTIVE;
	private int cooldownTicks;
	private ActionDefinition action;
	private boolean newRule = true;

	public static RuleDraft fromRuleDefinition(RuleDefinition ruleDefinition)
	{
		RuleDraft draft = new RuleDraft();
		draft.setId(ruleDefinition.getId());
		draft.setName(ruleDefinition.getName());
		draft.setEnabled(ruleDefinition.isEnabled());
		draft.setRootGroup(copyGroup(ruleDefinition.getRootGroup()));
		draft.setActivationMode(ruleDefinition.getActivationMode());
		draft.setCooldownTicks(ruleDefinition.getCooldownTicks());
		draft.setAction(copyAction(ruleDefinition.getAction()));
		draft.setNewRule(false);
		return draft;
	}

	public RuleDefinition toRuleDefinition()
	{
		RuleDefinition ruleDefinition = new RuleDefinition();
		ruleDefinition.setId(id);
		ruleDefinition.setName(name);
		ruleDefinition.setEnabled(enabled);
		ruleDefinition.setRootGroup(rootGroup);
		ruleDefinition.setActivationMode(activationMode);
		ruleDefinition.setCooldownTicks(cooldownTicks);
		ruleDefinition.setAction(action);
		return ruleDefinition;
	}

	public RuleDraft copy()
	{
		RuleDraft copy = new RuleDraft();
		copy.setId(id);
		copy.setName(name);
		copy.setEnabled(enabled);
		copy.setRootGroup(copyGroup(rootGroup));
		copy.setActivationMode(activationMode);
		copy.setCooldownTicks(cooldownTicks);
		copy.setAction(copyAction(action));
		copy.setNewRule(newRule);
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
