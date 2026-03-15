package nl.jessedezwart.strongbuffs.panel.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;

@Singleton
public class RuleDraftSession
{
	private RuleDraft draft;
	private String selectedRuleId;

	public RuleDraft getDraft()
	{
		return draft;
	}

	public String getSelectedRuleId()
	{
		return selectedRuleId;
	}

	public void clear()
	{
		draft = null;
		selectedRuleId = null;
	}

	public void select(RuleDefinition ruleDefinition)
	{
		selectedRuleId = ruleDefinition == null ? null : ruleDefinition.getId();
		draft = ruleDefinition == null ? null : RuleDraft.fromRuleDefinition(ruleDefinition);
	}

	public void createNew()
	{
		RuleDraft newDraft = new RuleDraft();
		newDraft.setId(UUID.randomUUID().toString());
		newDraft.setAction(new OverlayTextAction());
		newDraft.setRootGroup(new ConditionGroup());
		newDraft.setNewRule(true);
		draft = newDraft;
		selectedRuleId = newDraft.getId();
	}

	public void duplicate(RuleDefinition source)
	{
		if (source == null)
		{
			return;
		}

		RuleDraft duplicated = RuleDraft.fromRuleDefinition(source);
		duplicated.setId(UUID.randomUUID().toString());
		duplicated.setName(buildDuplicateName(source.getName()));
		duplicated.setNewRule(true);
		draft = duplicated;
		selectedRuleId = duplicated.getId();
	}

	public boolean hasUnsavedChanges(RuleDefinition persistedRule)
	{
		if (draft == null)
		{
			return false;
		}

		if (draft.isNewRule())
		{
			return true;
		}

		return persistedRule == null || !Objects.equals(persistedRule, draft.toRuleDefinition());
	}

	public List<RuleDefinition> getVisibleRules(List<RuleDefinition> persistedRules)
	{
		List<RuleDefinition> visible = new ArrayList<>(persistedRules);

		if (draft == null)
		{
			return Collections.unmodifiableList(visible);
		}

		RuleDefinition draftDefinition = draft.toRuleDefinition();
		int existingIndex = indexOf(visible, draftDefinition.getId());

		if (existingIndex >= 0)
		{
			visible.set(existingIndex, draftDefinition);
		}
		else
		{
			visible.add(draftDefinition);
		}

		return Collections.unmodifiableList(visible);
	}

	private static int indexOf(List<RuleDefinition> rules, String ruleId)
	{
		for (int i = 0; i < rules.size(); i++)
		{
			if (Objects.equals(rules.get(i).getId(), ruleId))
			{
				return i;
			}
		}

		return -1;
	}

	private static String buildDuplicateName(String name)
	{
		String baseName = name == null || name.trim().isEmpty() ? "New Rule" : name.trim();
		return baseName + " Copy";
	}
}
