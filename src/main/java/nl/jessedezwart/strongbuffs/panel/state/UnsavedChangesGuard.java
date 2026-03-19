package nl.jessedezwart.strongbuffs.panel.state;

import javax.inject.Singleton;

/**
 * Defers navigation-style actions until the user resolves unsaved changes.
 *
 * <p>The controller uses this guard so selection, creation, duplication, and deletion all share
 * the same save, discard, or cancel behavior.</p>
 */
@Singleton
public class UnsavedChangesGuard
{
	private PendingAction pendingAction;

	public RuleControllerActionResult runOrDefer(boolean hasUnsavedChanges, PendingAction action)
	{
		if (hasUnsavedChanges)
		{
			pendingAction = action;
			return RuleControllerActionResult.unsavedConfirmationRequired();
		}

		pendingAction = null;
		return action.run();
	}

	public RuleControllerActionResult resolve(UnsavedResolution resolution, PendingAction saveAction,
		PendingAction discardAction)
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
				if (discardAction != null)
				{
					discardAction.run();
				}
				break;
			case SAVE:
				if (saveAction != null)
				{
					RuleControllerActionResult saveResult = saveAction.run();

					if (!saveResult.isSuccess())
					{
						return saveResult;
					}
				}
				break;
			default:
				break;
		}

		PendingAction action = pendingAction;
		pendingAction = null;
		return action.run();
	}

	public void clear()
	{
		pendingAction = null;
	}

	@FunctionalInterface
	public interface PendingAction
	{
		RuleControllerActionResult run();
	}
}
