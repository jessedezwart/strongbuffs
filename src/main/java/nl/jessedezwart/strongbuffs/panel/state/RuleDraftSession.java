package nl.jessedezwart.strongbuffs.panel.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;

/**
 * Holds the currently selected draft rule and selection state for the sidebar.
 *
 * <p>The draft is intentionally separate from persisted rules so Swing components can mutate fields
 * freely without affecting saved data or runtime behavior until the user saves.</p>
 */
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

	/**
	 * Selects an existing persisted rule and clones it into an editable draft.
	 */
	public void select(RuleDefinition ruleDefinition)
	{
		selectedRuleId = ruleDefinition == null ? null : ruleDefinition.getId();
		draft = ruleDefinition == null ? null : RuleDraft.fromRuleDefinition(ruleDefinition);
	}

	/**
	 * Creates a new draft with safe defaults that are valid to render immediately.
	 */
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

	/**
	 * Duplicates the provided rule into a new unsaved draft with a fresh id.
	 */
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

		// The list view shows draft edits immediately, even before persistence, so selection and
		// unsaved-change prompts always reflect what the user currently sees.
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
