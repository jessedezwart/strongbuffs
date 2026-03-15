package nl.jessedezwart.strongbuffs.runtime.action;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

@Singleton
public class OverlayActionController
{
	private static final long TRANSIENT_DURATION_MILLIS = 1800L;

	private final RuntimeOverlay runtimeOverlay;
	private final RuntimeValueFormatter runtimeValueFormatter;

	private final Map<String, RuntimeOverlay.TextEntry> persistentEntries = new LinkedHashMap<>();
	private final Map<String, RuntimeOverlay.TextEntry> transientEntries = new LinkedHashMap<>();

	@Inject
	public OverlayActionController(RuntimeOverlay runtimeOverlay, RuntimeValueFormatter runtimeValueFormatter)
	{
		this.runtimeOverlay = runtimeOverlay;
		this.runtimeValueFormatter = runtimeValueFormatter;
	}

	public void showPersistent(CompiledRule rule, OverlayTextAction action, RuntimeState runtimeState)
	{
		persistentEntries.put(rule.getId(), buildEntry(rule, action, runtimeState, 0L));
		publish();
	}

	public void showTransient(CompiledRule rule, OverlayTextAction action, RuntimeState runtimeState)
	{
		long expiresAtMillis = System.currentTimeMillis() + TRANSIENT_DURATION_MILLIS;
		transientEntries.put(rule.getId(), buildEntry(rule, action, runtimeState, expiresAtMillis));
		publish();
	}

	public void removePersistent(String ruleId)
	{
		persistentEntries.remove(ruleId);
		publish();
	}

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

		if (action.isShowValue())
		{
			String value = runtimeValueFormatter.format(rule.getRootGroup(), runtimeState);

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
