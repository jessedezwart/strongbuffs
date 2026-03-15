package nl.jessedezwart.strongbuffs.runtime.action;

import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.impl.ScreenFlashAction;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

@Singleton
public class ScreenFlashActionHandler implements RuntimeActionHandler<ScreenFlashAction>
{
	private final ScreenFlashController screenFlashController;

	@Inject
	public ScreenFlashActionHandler(ScreenFlashController screenFlashController)
	{
		this.screenFlashController = screenFlashController;
	}

	@Override
	public Class<ScreenFlashAction> getActionType()
	{
		return ScreenFlashAction.class;
	}

	@Override
	public void activatePersistent(CompiledRule rule, ScreenFlashAction action, RuntimeState runtimeState)
	{
		screenFlashController.showPersistent(rule.getId(), action);
	}

	@Override
	public void updatePersistent(CompiledRule rule, ScreenFlashAction action, RuntimeState runtimeState)
	{
		screenFlashController.showPersistent(rule.getId(), action);
	}

	@Override
	public void deactivatePersistent(CompiledRule rule, ScreenFlashAction action)
	{
		screenFlashController.removePersistent(rule.getId());
	}

	@Override
	public void fireTransient(CompiledRule rule, ScreenFlashAction action, RuntimeState runtimeState)
	{
		screenFlashController.flashTransient(rule.getId(), action);
	}

	@Override
	public void clear()
	{
		screenFlashController.clear();
	}
}
