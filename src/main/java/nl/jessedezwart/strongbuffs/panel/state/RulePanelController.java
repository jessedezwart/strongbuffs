package nl.jessedezwart.strongbuffs.panel.state;

import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;

/**
 * Thin facade over repository, draft-session, validation, and unsaved-changes
 * services for the Swing panel.
 */
@Singleton
public class RulePanelController
{
	public static final String FIELD_NAME = "name";
	public static final String FIELD_CONDITIONS = "conditions";
	public static final String FIELD_ACTION = "action";
	public static final String FIELD_ACTION_TEXT = "action.text";
	public static final String FIELD_ACTION_COLOR = "action.color";

	private final RuleRepository repository;
	private final RuleDraftSession draftSession;
	private final RuleDraftValidator draftValidator;
	private final UnsavedChangesGuard unsavedChangesGuard;

	private RuleValidationResult validationResult = RuleValidationResult.valid();

	@Inject
	public RulePanelController(RuleRepository repository, RuleDraftSession draftSession,
		RuleDraftValidator draftValidator, UnsavedChangesGuard unsavedChangesGuard)
	{
		this.repository = repository;
		this.draftSession = draftSession;
		this.draftValidator = draftValidator;
		this.unsavedChangesGuard = unsavedChangesGuard;
		reload();
	}

	public void reload()
	{
		repository.reload();
		draftSession.clear();
		validationResult = RuleValidationResult.valid();
		unsavedChangesGuard.clear();
	}

	public List<RuleDefinition> getVisibleRules()
	{
		return draftSession.getVisibleRules(repository.getAll());
	}

	public RuleDraft getDraft()
	{
		return draftSession.getDraft();
	}

	public String getSelectedRuleId()
	{
		return draftSession.getSelectedRuleId();
	}

	public RuleValidationResult getValidationResult()
	{
		return validationResult;
	}

	public boolean hasUnsavedChanges()
	{
		RuleDraft draft = draftSession.getDraft();
		return draft != null && draftSession.hasUnsavedChanges(repository.findById(draft.getId()));
	}

	public RuleControllerActionResult revalidateDraft()
	{
		validationResult = draftValidator.validate(draftSession.getDraft());
		return validationResult.isValid()
			? RuleControllerActionResult.applied()
			: RuleControllerActionResult.validationFailed(validationResult);
	}

	public RuleControllerActionResult requestSelectRule(String ruleId)
	{
		if (Objects.equals(getSelectedRuleId(), ruleId))
		{
			return RuleControllerActionResult.applied();
		}

		return unsavedChangesGuard.runOrDefer(hasUnsavedChanges(), () -> selectRuleInternal(ruleId));
	}

	public RuleControllerActionResult requestCreateRule()
	{
		return unsavedChangesGuard.runOrDefer(hasUnsavedChanges(), this::createRuleInternal);
	}

	public RuleControllerActionResult requestDuplicateSelectedRule()
	{
		if (getSelectedRuleId() == null)
		{
			return RuleControllerActionResult.applied();
		}

		return unsavedChangesGuard.runOrDefer(hasUnsavedChanges(), this::duplicateSelectedRuleInternal);
	}

	public RuleControllerActionResult requestDeleteSelectedRule()
	{
		if (getSelectedRuleId() == null)
		{
			return RuleControllerActionResult.applied();
		}

		RuleDraft draft = draftSession.getDraft();

		if (draft != null && draft.isNewRule() && Objects.equals(draft.getId(), getSelectedRuleId()))
		{
			draftSession.clear();
			validationResult = RuleValidationResult.valid();
			unsavedChangesGuard.clear();
			return RuleControllerActionResult.applied();
		}

		return unsavedChangesGuard.runOrDefer(hasUnsavedChanges(), this::deleteSelectedRuleInternal);
	}

	public RuleControllerActionResult resolvePendingAction(UnsavedResolution resolution)
	{
		return unsavedChangesGuard.resolve(resolution, this::saveDraft, this::discardDraftInternal);
	}

	public RuleControllerActionResult saveDraft()
	{
		RuleDraft draft = draftSession.getDraft();

		if (draft == null)
		{
			return RuleControllerActionResult.applied();
		}

		validationResult = draftValidator.validate(draft);

		if (!validationResult.isValid())
		{
			return RuleControllerActionResult.validationFailed(validationResult);
		}

		draft.setName(draft.getName().trim());
		RuleDefinition savedRule = draft.toRuleDefinition();
		repository.saveRule(savedRule);
		draftSession.select(savedRule);
		validationResult = RuleValidationResult.valid();
		return RuleControllerActionResult.applied();
	}

	public void cancelDraft()
	{
		unsavedChangesGuard.clear();
		discardDraftInternal();
	}

	private RuleControllerActionResult selectRuleInternal(String ruleId)
	{
		draftSession.select(repository.findById(ruleId));
		validationResult = RuleValidationResult.valid();
		return RuleControllerActionResult.applied();
	}

	private RuleControllerActionResult createRuleInternal()
	{
		draftSession.createNew();
		validationResult = draftValidator.validate(draftSession.getDraft());
		return RuleControllerActionResult.applied();
	}

	private RuleControllerActionResult duplicateSelectedRuleInternal()
	{
		draftSession.duplicate(getSelectedRuleSnapshot());
		validationResult = draftValidator.validate(draftSession.getDraft());
		return RuleControllerActionResult.applied();
	}

	private RuleControllerActionResult deleteSelectedRuleInternal()
	{
		repository.deleteRule(getSelectedRuleId());
		draftSession.clear();
		validationResult = RuleValidationResult.valid();
		return RuleControllerActionResult.applied();
	}

	private RuleControllerActionResult discardDraftInternal()
	{
		RuleDraft draft = draftSession.getDraft();

		if (draft == null)
		{
			return RuleControllerActionResult.applied();
		}

		if (draft.isNewRule())
		{
			draftSession.clear();
		}
		else
		{
			draftSession.select(repository.findById(draft.getId()));
		}

		validationResult = RuleValidationResult.valid();
		return RuleControllerActionResult.applied();
	}

	private RuleDefinition getSelectedRuleSnapshot()
	{
		RuleDraft draft = draftSession.getDraft();
		return draft != null ? draft.toRuleDefinition() : repository.findById(getSelectedRuleId());
	}
}
