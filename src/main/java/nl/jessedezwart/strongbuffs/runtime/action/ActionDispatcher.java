package nl.jessedezwart.strongbuffs.runtime.action;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.OverlayManager;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

@Singleton
public class ActionDispatcher
{
	private final OverlayManager overlayManager;
	private final RuntimeOverlay runtimeOverlay;
	private final RuntimeActionHandlerRegistry runtimeActionHandlerRegistry;

	@Inject
	public ActionDispatcher(OverlayManager overlayManager, RuntimeOverlay runtimeOverlay,
		RuntimeActionHandlerRegistry runtimeActionHandlerRegistry)
	{
		this.overlayManager = overlayManager;
		this.runtimeOverlay = runtimeOverlay;
		this.runtimeActionHandlerRegistry = runtimeActionHandlerRegistry;
	}

	public void startUp()
	{
		overlayManager.add(runtimeOverlay);
		runtimeActionHandlerRegistry.startUp();
		clearAll();
	}

	public void shutDown()
	{
		clearAll();
		overlayManager.remove(runtimeOverlay);
		runtimeActionHandlerRegistry.shutDown();
	}

	public void activatePersistent(CompiledRule rule, RuntimeState runtimeState)
	{
		runtimeActionHandlerRegistry.activatePersistent(rule, runtimeState);
	}

	public void updatePersistent(CompiledRule rule, RuntimeState runtimeState)
	{
		runtimeActionHandlerRegistry.updatePersistent(rule, runtimeState);
	}

	public void deactivatePersistent(CompiledRule rule)
	{
		runtimeActionHandlerRegistry.deactivatePersistent(rule);
	}

	public void fireTransient(CompiledRule rule, RuntimeState runtimeState)
	{
		runtimeActionHandlerRegistry.fireTransient(rule, runtimeState);
	}

	public void clearAll()
	{
		runtimeActionHandlerRegistry.clear();
	}
}
