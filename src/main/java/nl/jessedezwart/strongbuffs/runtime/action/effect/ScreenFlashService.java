package nl.jessedezwart.strongbuffs.runtime.action.effect;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.impl.ScreenFlashAction;
import nl.jessedezwart.strongbuffs.runtime.action.RuntimeActionHandler;
import nl.jessedezwart.strongbuffs.runtime.action.RuntimeOverlay;
import nl.jessedezwart.strongbuffs.runtime.engine.CompiledRule;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

/**
 * Manages the set of screen flash/tint entries displayed by the {@link RuntimeOverlay}.
 *
 * <p>Persistent flashes use a subtle alpha ({@value #PERSISTENT_ALPHA}) and stay visible as
 * long as their rule is active. Transient flashes use a stronger alpha ({@value #TRANSIENT_ALPHA})
 * and expire after a configurable number of game ticks. On every mutation the full flash list is
 * republished to the overlay.</p>
 */
@Singleton
public class ScreenFlashService implements RuntimeActionHandler<ScreenFlashAction>
{
	private static final float PERSISTENT_ALPHA = 0.15f;
	private static final float TRANSIENT_ALPHA = 0.28f;

	private final RuntimeOverlay runtimeOverlay;

	private final Map<String, RuntimeOverlay.FlashEntry> persistentFlashes = new LinkedHashMap<>();
	private final Map<String, RuntimeOverlay.FlashEntry> transientFlashes = new LinkedHashMap<>();

	@Inject
	public ScreenFlashService(RuntimeOverlay runtimeOverlay)
	{
		this.runtimeOverlay = runtimeOverlay;
	}

	@Override
	public Class<ScreenFlashAction> getActionType()
	{
		return ScreenFlashAction.class;
	}

	@Override
	public void activatePersistent(CompiledRule rule, ScreenFlashAction action, RuntimeState runtimeState)
	{
		showPersistent(rule.getId(), action);
	}

	@Override
	public void updatePersistent(CompiledRule rule, ScreenFlashAction action, RuntimeState runtimeState)
	{
		showPersistent(rule.getId(), action);
	}

	@Override
	public void deactivatePersistent(CompiledRule rule, ScreenFlashAction action)
	{
		removePersistent(rule.getId());
	}

	@Override
	public void fireTransient(CompiledRule rule, ScreenFlashAction action, RuntimeState runtimeState)
	{
		flashTransient(rule.getId(), action);
	}

	private void showPersistent(String ruleId, ScreenFlashAction action)
	{
		persistentFlashes.put(ruleId, new RuntimeOverlay.FlashEntry(parseColor(action.getColorHex()), PERSISTENT_ALPHA, 0L));
		publish();
	}

	private void flashTransient(String ruleId, ScreenFlashAction action)
	{
		long expiresAtMillis = System.currentTimeMillis() + (Math.max(1, action.getDurationTicks()) * 600L);
		transientFlashes.put(ruleId, new RuntimeOverlay.FlashEntry(parseColor(action.getColorHex()), TRANSIENT_ALPHA,
			expiresAtMillis));
		publish();
	}

	private void removePersistent(String ruleId)
	{
		persistentFlashes.remove(ruleId);
		publish();
	}

	@Override
	public void clear()
	{
		persistentFlashes.clear();
		transientFlashes.clear();
		publish();
	}

	private void publish()
	{
		long now = System.currentTimeMillis();
		transientFlashes.entrySet().removeIf(entry -> entry.getValue().isExpired(now));

		List<RuntimeOverlay.FlashEntry> flashes = new ArrayList<>(persistentFlashes.values());
		flashes.addAll(transientFlashes.values());
		runtimeOverlay.setFlashEntries(flashes);
	}

	private static Color parseColor(String colorHex)
	{
		try
		{
			return Color.decode(colorHex);
		}
		catch (RuntimeException ex)
		{
			return Color.RED;
		}
	}
}
