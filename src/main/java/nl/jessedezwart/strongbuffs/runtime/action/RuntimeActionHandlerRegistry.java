package nl.jessedezwart.strongbuffs.runtime.action;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

@Singleton
public class RuntimeActionHandlerRegistry
{
	private final Map<Class<? extends ActionDefinition>, RuntimeActionHandler<? extends ActionDefinition>> handlers =
		new LinkedHashMap<>();

	@Inject
	public RuntimeActionHandlerRegistry(OverlayTextActionHandler overlayTextActionHandler,
		ScreenFlashActionHandler screenFlashActionHandler, SoundAlertActionHandler soundAlertActionHandler)
	{
		register(overlayTextActionHandler);
		register(screenFlashActionHandler);
		register(soundAlertActionHandler);
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
