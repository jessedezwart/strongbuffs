package nl.jessedezwart.strongbuffs.runtime.action;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.OverlayManager;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.action.impl.ScreenFlashAction;
import nl.jessedezwart.strongbuffs.model.action.impl.SoundAlertAction;
import nl.jessedezwart.strongbuffs.runtime.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.RuntimeState;

@Singleton
public class ActionDispatcher
{
	private final OverlayManager overlayManager;
	private final RuntimeOverlay runtimeOverlay;
	private final OverlayActionController overlayActionController;
	private final ScreenFlashController screenFlashController;
	private final SoundAlertController soundAlertController;

	@Inject
	public ActionDispatcher(OverlayManager overlayManager, RuntimeOverlay runtimeOverlay,
		OverlayActionController overlayActionController, ScreenFlashController screenFlashController,
		SoundAlertController soundAlertController)
	{
		this.overlayManager = overlayManager;
		this.runtimeOverlay = runtimeOverlay;
		this.overlayActionController = overlayActionController;
		this.screenFlashController = screenFlashController;
		this.soundAlertController = soundAlertController;
	}

	public void startUp()
	{
		overlayManager.add(runtimeOverlay);
		clearAll();
	}

	public void shutDown()
	{
		clearAll();
		overlayManager.remove(runtimeOverlay);
		soundAlertController.shutDown();
	}

	public void activatePersistent(CompiledRule rule, RuntimeState runtimeState)
	{
		ActionDefinition action = rule.getAction();

		if (action instanceof OverlayTextAction)
		{
			overlayActionController.showPersistent(rule, (OverlayTextAction) action, runtimeState);
			return;
		}

		if (action instanceof ScreenFlashAction)
		{
			screenFlashController.showPersistent(rule.getId(), (ScreenFlashAction) action);
			return;
		}

		if (action instanceof SoundAlertAction)
		{
			soundAlertController.play((SoundAlertAction) action);
		}
	}

	public void updatePersistent(CompiledRule rule, RuntimeState runtimeState)
	{
		ActionDefinition action = rule.getAction();

		if (action instanceof OverlayTextAction)
		{
			overlayActionController.showPersistent(rule, (OverlayTextAction) action, runtimeState);
		}
		else if (action instanceof ScreenFlashAction)
		{
			screenFlashController.showPersistent(rule.getId(), (ScreenFlashAction) action);
		}
	}

	public void deactivatePersistent(CompiledRule rule)
	{
		ActionDefinition action = rule.getAction();

		if (action instanceof OverlayTextAction)
		{
			overlayActionController.removePersistent(rule.getId());
		}
		else if (action instanceof ScreenFlashAction)
		{
			screenFlashController.removePersistent(rule.getId());
		}
	}

	public void fireTransient(CompiledRule rule, RuntimeState runtimeState)
	{
		ActionDefinition action = rule.getAction();

		if (action instanceof OverlayTextAction)
		{
			overlayActionController.showTransient(rule, (OverlayTextAction) action, runtimeState);
			return;
		}

		if (action instanceof ScreenFlashAction)
		{
			screenFlashController.flashTransient(rule.getId(), (ScreenFlashAction) action);
			return;
		}

		if (action instanceof SoundAlertAction)
		{
			soundAlertController.play((SoundAlertAction) action);
		}
	}

	public void clearAll()
	{
		overlayActionController.clear();
		screenFlashController.clear();
	}
}
