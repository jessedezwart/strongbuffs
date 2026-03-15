package nl.jessedezwart.strongbuffs.panel.state;

/**
 * Result object for controller actions that may succeed immediately, require validation fixes, or require unsaved-changes confirmation.
 */
public final class RuleControllerActionResult
{
	private static final RuleControllerActionResult APPLIED = new RuleControllerActionResult(false, RuleValidationResult.valid());
	private static final RuleControllerActionResult UNSAVED_CONFIRMATION_REQUIRED =
		new RuleControllerActionResult(true, RuleValidationResult.valid());

	private final boolean requiresUnsavedConfirmation;
	private final RuleValidationResult validationResult;

	private RuleControllerActionResult(boolean requiresUnsavedConfirmation, RuleValidationResult validationResult)
	{
		this.requiresUnsavedConfirmation = requiresUnsavedConfirmation;
		this.validationResult = validationResult;
	}

	public static RuleControllerActionResult applied()
	{
		return APPLIED;
	}

	public static RuleControllerActionResult unsavedConfirmationRequired()
	{
		return UNSAVED_CONFIRMATION_REQUIRED;
	}

	public static RuleControllerActionResult validationFailed(RuleValidationResult validationResult)
	{
		return new RuleControllerActionResult(false, validationResult);
	}

	public boolean requiresUnsavedConfirmation()
	{
		return requiresUnsavedConfirmation;
	}

	public RuleValidationResult getValidationResult()
	{
		return validationResult;
	}

	public boolean isSuccess()
	{
		return !requiresUnsavedConfirmation && validationResult.isValid();
	}
}
