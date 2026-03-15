package nl.jessedezwart.strongbuffs.runtime.action;

import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

@Singleton
public class OverlayTextActionHandler implements RuntimeActionHandler<OverlayTextAction>
{
	private final OverlayActionController overlayActionController;

	@Inject
	public OverlayTextActionHandler(OverlayActionController overlayActionController)
	{
		this.overlayActionController = overlayActionController;
	}

	@Override
	public Class<OverlayTextAction> getActionType()
	{
		return OverlayTextAction.class;
	}

	@Override
	public void activatePersistent(CompiledRule rule, OverlayTextAction action, RuntimeState runtimeState)
	{
		overlayActionController.showPersistent(rule, action, runtimeState);
	}

	@Override
	public void updatePersistent(CompiledRule rule, OverlayTextAction action, RuntimeState runtimeState)
	{
		overlayActionController.showPersistent(rule, action, runtimeState);
	}

	@Override
	public void deactivatePersistent(CompiledRule rule, OverlayTextAction action)
	{
		overlayActionController.removePersistent(rule.getId());
	}

	@Override
	public void fireTransient(CompiledRule rule, OverlayTextAction action, RuntimeState runtimeState)
	{
		overlayActionController.showTransient(rule, action, runtimeState);
	}

	@Override
	public void clear()
	{
		overlayActionController.clear();
	}
}
