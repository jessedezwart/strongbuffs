package nl.jessedezwart.strongbuffs.panel.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.RuleDefinitionStore;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.panel.editor.ActionEditorRegistry;
import nl.jessedezwart.strongbuffs.panel.editor.ConditionEditorRegistry;

/**
 * Owns persisted rule snapshots, the currently selected draft, validation, and unsaved-changes resolution.
 */
@Singleton
public class RulePanelController
{
	public static final String FIELD_NAME = "name";
	public static final String FIELD_CONDITIONS = "conditions";
	public static final String FIELD_ACTION = "action";
	public static final String FIELD_ACTION_TEXT = "action.text";
	public static final String FIELD_ACTION_COLOR = "action.color";

	private final RuleDefinitionStore store;
	private final ConditionEditorRegistry conditionRegistry;
	private final ActionEditorRegistry actionRegistry;

	private final List<RuleDefinition> persistedRules = new ArrayList<>();

	private RuleDraft draft;
	private String selectedRuleId;
	private RuleValidationResult validationResult = RuleValidationResult.valid();
	private PendingAction pendingAction;

	@Inject
	public RulePanelController(RuleDefinitionStore store, ConditionEditorRegistry conditionRegistry,
		ActionEditorRegistry actionRegistry)
	{
		this.store = store;
		this.conditionRegistry = conditionRegistry;
		this.actionRegistry = actionRegistry;
		reload();
	}

	public void reload()
	{
		persistedRules.clear();
		persistedRules.addAll(store.load());
		draft = null;
		selectedRuleId = null;
		validationResult = RuleValidationResult.valid();
		pendingAction = null;
	}

	public List<RuleDefinition> getVisibleRules()
	{
		List<RuleDefinition> visible = new ArrayList<>(persistedRules);

		if (draft == null)
		{
			return Collections.unmodifiableList(visible);
		}

		RuleDefinition draftDefinition = draft.toRuleDefinition();
		int existingIndex = indexOfRule(draftDefinition.getId());

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

	public RuleDraft getDraft()
	{
		return draft;
	}

	public String getSelectedRuleId()
	{
		return selectedRuleId;
	}

	public RuleValidationResult getValidationResult()
	{
		return validationResult;
	}

	public boolean hasUnsavedChanges()
	{
		if (draft == null)
		{
			return false;
		}

		if (draft.isNewRule())
		{
			return true;
		}

		RuleDefinition persisted = getPersistedRule(draft.getId());
		return persisted == null || !Objects.equals(persisted, draft.toRuleDefinition());
	}

	public RuleControllerActionResult revalidateDraft()
	{
		validationResult = validateDraft();

		if (validationResult.isValid())
		{
			return RuleControllerActionResult.applied();
		}

		return RuleControllerActionResult.validationFailed(validationResult);
	}

	public RuleControllerActionResult requestSelectRule(String ruleId)
	{
		if (Objects.equals(selectedRuleId, ruleId))
		{
			return RuleControllerActionResult.applied();
		}

		return runOrDefer(() -> selectRuleInternal(ruleId));
	}

	public RuleControllerActionResult requestCreateRule()
	{
		return runOrDefer(this::createRuleInternal);
	}

	public RuleControllerActionResult requestDuplicateSelectedRule()
	{
		if (selectedRuleId == null)
		{
			return RuleControllerActionResult.applied();
		}

		return runOrDefer(this::duplicateSelectedRuleInternal);
	}

	public RuleControllerActionResult requestDeleteSelectedRule()
	{
		if (selectedRuleId == null)
		{
			return RuleControllerActionResult.applied();
		}

		if (draft != null && draft.isNewRule() && Objects.equals(draft.getId(), selectedRuleId))
		{
			draft = null;
			selectedRuleId = null;
			validationResult = RuleValidationResult.valid();
			pendingAction = null;
			return RuleControllerActionResult.applied();
		}

		return runOrDefer(this::deleteSelectedRuleInternal);
	}

	public RuleControllerActionResult resolvePendingAction(UnsavedResolution resolution)
	{
		if (pendingAction == null)
		{
			return RuleControllerActionResult.applied();
		}

		switch (resolution)
		{
			case CANCEL:
				pendingAction = null;
				return RuleControllerActionResult.applied();
			case DISCARD:
				discardDraft();
				break;
			case SAVE:
				RuleControllerActionResult saveResult = saveDraft();

				if (!saveResult.isSuccess())
				{
					return saveResult;
				}
				break;
			default:
				break;
		}

		PendingAction action = pendingAction;
		pendingAction = null;
		return action.run();
	}

	public RuleControllerActionResult saveDraft()
	{
		if (draft == null)
		{
			return RuleControllerActionResult.applied();
		}

		validationResult = validateDraft();

		if (!validationResult.isValid())
		{
			return RuleControllerActionResult.validationFailed(validationResult);
		}

		draft.setName(draft.getName().trim());
		RuleDefinition savedRule = draft.toRuleDefinition();
		int existingIndex = indexOfRule(savedRule.getId());

		if (existingIndex >= 0)
		{
			persistedRules.set(existingIndex, savedRule);
		}
		else
		{
			persistedRules.add(savedRule);
		}

		store.save(new ArrayList<>(persistedRules));
		draft = RuleDraft.fromRuleDefinition(savedRule);
		selectedRuleId = savedRule.getId();
		validationResult = RuleValidationResult.valid();
		return RuleControllerActionResult.applied();
	}

	public void cancelDraft()
	{
		pendingAction = null;
		discardDraft();
	}

	private void discardDraft()
	{
		if (draft == null)
		{
			return;
		}

		if (draft.isNewRule())
		{
			draft = null;
			selectedRuleId = null;
			validationResult = RuleValidationResult.valid();
			return;
		}

		selectRuleInternal(draft.getId());
	}

	private RuleControllerActionResult runOrDefer(PendingAction action)
	{
		if (hasUnsavedChanges())
		{
			pendingAction = action;
			return RuleControllerActionResult.unsavedConfirmationRequired();
		}

		pendingAction = null;
		return action.run();
	}

	private RuleControllerActionResult selectRuleInternal(String ruleId)
	{
		selectedRuleId = ruleId;
		RuleDefinition ruleDefinition = getPersistedRule(ruleId);

		if (ruleDefinition == null)
		{
			draft = null;
			validationResult = RuleValidationResult.valid();
			return RuleControllerActionResult.applied();
		}

		draft = RuleDraft.fromRuleDefinition(ruleDefinition);
		validationResult = RuleValidationResult.valid();
		return RuleControllerActionResult.applied();
	}

	private RuleControllerActionResult createRuleInternal()
	{
		RuleDraft newDraft = new RuleDraft();
		newDraft.setId(UUID.randomUUID().toString());
		newDraft.setAction(new OverlayTextAction());
		newDraft.setRootGroup(new ConditionGroup());
		newDraft.setNewRule(true);
		draft = newDraft;
		selectedRuleId = newDraft.getId();
		validationResult = validateDraft();
		return RuleControllerActionResult.applied();
	}

	private RuleControllerActionResult duplicateSelectedRuleInternal()
	{
		RuleDefinition source = getSelectedRuleSnapshot();

		if (source == null)
		{
			return RuleControllerActionResult.applied();
		}

		RuleDraft duplicated = RuleDraft.fromRuleDefinition(source);
		duplicated.setId(UUID.randomUUID().toString());
		duplicated.setName(buildDuplicateName(source.getName()));
		duplicated.setNewRule(true);
		draft = duplicated;
		selectedRuleId = duplicated.getId();
		validationResult = validateDraft();
		return RuleControllerActionResult.applied();
	}

	private RuleControllerActionResult deleteSelectedRuleInternal()
	{
		int existingIndex = indexOfRule(selectedRuleId);

		if (existingIndex < 0)
		{
			return RuleControllerActionResult.applied();
		}

		persistedRules.remove(existingIndex);
		store.save(new ArrayList<>(persistedRules));
		draft = null;
		selectedRuleId = null;
		validationResult = RuleValidationResult.valid();
		return RuleControllerActionResult.applied();
	}

	private RuleDefinition getSelectedRuleSnapshot()
	{
		if (draft != null)
		{
			return draft.toRuleDefinition();
		}

		return getPersistedRule(selectedRuleId);
	}

	private RuleDefinition getPersistedRule(String ruleId)
	{
		if (ruleId == null)
		{
			return null;
		}

		for (RuleDefinition ruleDefinition : persistedRules)
		{
			if (Objects.equals(ruleDefinition.getId(), ruleId))
			{
				return ruleDefinition;
			}
		}

		return null;
	}

	private int indexOfRule(String ruleId)
	{
		for (int i = 0; i < persistedRules.size(); i++)
		{
			if (Objects.equals(persistedRules.get(i).getId(), ruleId))
			{
				return i;
			}
		}

		return -1;
	}

	private RuleValidationResult validateDraft()
	{
		if (draft == null)
		{
			return RuleValidationResult.valid();
		}

		Map<String, String> errors = new LinkedHashMap<>();

		if (draft.getName() == null || draft.getName().trim().isEmpty())
		{
			errors.put(FIELD_NAME, "Name is required.");
		}

		if (!hasLeafCondition(draft.getRootGroup()))
		{
			errors.put(FIELD_CONDITIONS, "Add at least one condition.");
		}

		if (draft.getCooldownTicks() < 0)
		{
			errors.put("activation.cooldownTicks", "Cooldown must be zero or higher.");
		}

		validateAction(errors, draft.getAction());
		return RuleValidationResult.of(errors);
	}

	private void validateAction(Map<String, String> errors, ActionDefinition actionDefinition)
	{
		if (actionDefinition == null)
		{
			errors.put(FIELD_ACTION, "Choose an action.");
			return;
		}

		actionDefinition.validate(errors, FIELD_ACTION);
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

	private static String buildDuplicateName(String name)
	{
		String baseName = name == null || name.trim().isEmpty() ? "New Rule" : name.trim();
		return baseName + " Copy";
	}

	private interface PendingAction
	{
		RuleControllerActionResult run();
	}
}
