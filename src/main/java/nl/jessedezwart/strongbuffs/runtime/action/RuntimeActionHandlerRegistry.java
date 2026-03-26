package nl.jessedezwart.strongbuffs.runtime.action;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.runtime.action.effect.OverlayTextService;
import nl.jessedezwart.strongbuffs.runtime.action.effect.ScreenFlashService;
import nl.jessedezwart.strongbuffs.runtime.action.effect.SoundAlertService;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

/**
 * Routes action lifecycle calls to the correct {@link RuntimeActionHandler} based on the
 * concrete {@link ActionDefinition} type attached to a compiled rule.
 *
 * <p>All known handlers are registered at construction time. At runtime, the registry looks up the
 * handler for a rule's action definition class and delegates the persistent/transient call to it.</p>
 */
@Singleton
public class RuntimeActionHandlerRegistry
{
	private final Map<Class<? extends ActionDefinition>, RuntimeActionHandler<? extends ActionDefinition>> handlers =
		new LinkedHashMap<>();

	@Inject
	public RuntimeActionHandlerRegistry(OverlayTextService overlayTextService,
		ScreenFlashService screenFlashService, SoundAlertService soundAlertService)
	{
		register(overlayTextService);
		register(screenFlashService);
		register(soundAlertService);
	}

	public void startUp()
	{
		for (RuntimeActionHandler<? extends ActionDefinition> handler : handlers.values())
		{
			handler.startUp();
		}
	}

	public void shutDown()
	{
		for (RuntimeActionHandler<? extends ActionDefinition> handler : handlers.values())
		{
			handler.shutDown();
		}
	}

	public void clear()
	{
		for (RuntimeActionHandler<? extends ActionDefinition> handler : handlers.values())
		{
			handler.clear();
		}
	}

	public void activatePersistent(CompiledRule rule, RuntimeState runtimeState)
	{
		dispatch(rule.getAction()).activatePersistent(rule, rule.getAction(), runtimeState);
	}

	public void updatePersistent(CompiledRule rule, RuntimeState runtimeState)
	{
		dispatch(rule.getAction()).updatePersistent(rule, rule.getAction(), runtimeState);
	}

	public void deactivatePersistent(CompiledRule rule)
	{
		dispatch(rule.getAction()).deactivatePersistent(rule, rule.getAction());
	}

	public void fireTransient(CompiledRule rule, RuntimeState runtimeState)
	{
		dispatch(rule.getAction()).fireTransient(rule, rule.getAction(), runtimeState);
	}

	private <T extends ActionDefinition> void register(RuntimeActionHandler<T> handler)
	{
		handlers.put(handler.getActionType(), handler);
	}

	@SuppressWarnings("unchecked")
	private <T extends ActionDefinition> RuntimeActionHandler<T> dispatch(ActionDefinition actionDefinition)
	{
		if (actionDefinition == null)
		{
			throw new IllegalArgumentException("Action definition is required");
		}

		RuntimeActionHandler<?> handler = handlers.get(actionDefinition.getClass());

		if (handler == null)
		{
			throw new IllegalArgumentException("Unsupported action definition: " + actionDefinition.getClass());
		}

		return (RuntimeActionHandler<T>) handler;
	}
}
