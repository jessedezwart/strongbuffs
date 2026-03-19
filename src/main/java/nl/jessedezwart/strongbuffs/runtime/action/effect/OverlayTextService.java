package nl.jessedezwart.strongbuffs.runtime.action.effect;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.ConditionNode;
import nl.jessedezwart.strongbuffs.runtime.action.RuntimeActionHandler;
import nl.jessedezwart.strongbuffs.runtime.action.RuntimeOverlay;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionRuntimeAdapterRegistry;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

/**
 * Manages the set of text entries displayed by the {@link RuntimeOverlay}.
 *
 * <p>
 * Persistent entries remain visible as long as their rule is active. Transient
 * entries auto-expire after {@value #TRANSIENT_DURATION_MILLIS}ms. On every
 * mutation the full entry list is republished to the overlay so it always
 * renders the current set.
 * </p>
 *
 * <p>
 * When "show value" is enabled on an action, the service walks the rule's
 * condition tree to find the first leaf condition and formats its live value
 * (e.g. "43 hp") via {@link ConditionRuntimeAdapterRegistry#formatValue}.
 * </p>
 */
@Singleton
public class OverlayTextService implements RuntimeActionHandler<OverlayTextAction>
{
	private static final long TRANSIENT_DURATION_MILLIS = 1800L;

	private final RuntimeOverlay runtimeOverlay;
	private final ConditionRuntimeAdapterRegistry conditionRuntimeRegistry;

	private final Map<String, RuntimeOverlay.TextEntry> persistentEntries = new LinkedHashMap<>();
	private final Map<String, RuntimeOverlay.TextEntry> transientEntries = new LinkedHashMap<>();

	@Inject
	public OverlayTextService(RuntimeOverlay runtimeOverlay, ConditionRuntimeAdapterRegistry conditionRuntimeRegistry)
	{
		this.runtimeOverlay = runtimeOverlay;
		this.conditionRuntimeRegistry = conditionRuntimeRegistry;
	}

	@Override
	public Class<OverlayTextAction> getActionType()
	{
		return OverlayTextAction.class;
	}

	@Override
	public void activatePersistent(CompiledRule rule, OverlayTextAction action, RuntimeState runtimeState)
	{
		showPersistent(rule, action, runtimeState);
	}

	@Override
	public void updatePersistent(CompiledRule rule, OverlayTextAction action, RuntimeState runtimeState)
	{
		showPersistent(rule, action, runtimeState);
	}

	@Override
	public void deactivatePersistent(CompiledRule rule, OverlayTextAction action)
	{
		removePersistent(rule.getId());
	}

	@Override
	public void fireTransient(CompiledRule rule, OverlayTextAction action, RuntimeState runtimeState)
	{
		showTransient(rule, action, runtimeState);
	}

	private void showPersistent(CompiledRule rule, OverlayTextAction action, RuntimeState runtimeState)
	{
		persistentEntries.put(rule.getId(), buildEntry(rule, action, runtimeState, 0L));
		publish();
	}

	private void showTransient(CompiledRule rule, OverlayTextAction action, RuntimeState runtimeState)
	{
		long expiresAtMillis = System.currentTimeMillis() + TRANSIENT_DURATION_MILLIS;
		transientEntries.put(rule.getId(), buildEntry(rule, action, runtimeState, expiresAtMillis));
		publish();
	}

	private void removePersistent(String ruleId)
	{
		persistentEntries.remove(ruleId);
		publish();
	}

	@Override
	public void clear()
	{
		persistentEntries.clear();
		transientEntries.clear();
		publish();
	}

	private RuntimeOverlay.TextEntry buildEntry(CompiledRule rule, OverlayTextAction action, RuntimeState runtimeState,
			long expiresAtMillis)
	{
		String text = action.getText() == null ? "" : action.getText().trim();

		if (action.getShowValue())
		{
			String value = formatRuntimeValue(rule.getRootGroup(), runtimeState);

			if (value != null && !value.isBlank())
			{
				text = text + ": " + value;
			}
		}

		if (text.isBlank())
		{
			text = rule.getName() == null ? "Rule active" : rule.getName();
		}

		return new RuntimeOverlay.TextEntry(text, parseColor(action.getColorHex()), expiresAtMillis);
	}

	private String formatRuntimeValue(ConditionGroup rootGroup, RuntimeState runtimeState)
	{
		return conditionRuntimeRegistry.formatValue(findPrimaryCondition(rootGroup), runtimeState);
	}

	private ConditionDefinition findPrimaryCondition(ConditionNode node)
	{
		if (node instanceof ConditionDefinition)
		{
			return (ConditionDefinition) node;
		}

		if (node instanceof ConditionGroup)
		{
			for (ConditionNode child : ((ConditionGroup) node).getChildren())
			{
				ConditionDefinition primary = findPrimaryCondition(child);

				if (primary != null)
				{
					return primary;
				}
			}
		}

		return null;
	}

	private void publish()
	{
		long now = System.currentTimeMillis();
		transientEntries.entrySet().removeIf(entry -> entry.getValue().isExpired(now));

		List<RuntimeOverlay.TextEntry> entries = new ArrayList<>(persistentEntries.values());
		entries.addAll(transientEntries.values());
		runtimeOverlay.setTextEntries(entries);
	}

	private static Color parseColor(String colorHex)
	{
		try
		{
			return Color.decode(colorHex);
		}
		catch (RuntimeException ex)
		{
			return Color.WHITE;
		}
	}
}
