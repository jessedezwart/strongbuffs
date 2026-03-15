package nl.jessedezwart.strongbuffs.runtime.action;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.model.action.impl.ScreenFlashAction;

@Singleton
public class ScreenFlashController
{
	private static final float PERSISTENT_ALPHA = 0.15f;
	private static final float TRANSIENT_ALPHA = 0.28f;

	private final RuntimeOverlay runtimeOverlay;

	private final Map<String, RuntimeOverlay.FlashEntry> persistentFlashes = new LinkedHashMap<>();
	private final Map<String, RuntimeOverlay.FlashEntry> transientFlashes = new LinkedHashMap<>();

	@Inject
	public ScreenFlashController(RuntimeOverlay runtimeOverlay)
	{
		this.runtimeOverlay = runtimeOverlay;
	}

	public void showPersistent(String ruleId, ScreenFlashAction action)
	{
		persistentFlashes.put(ruleId, new RuntimeOverlay.FlashEntry(parseColor(action.getColorHex()), PERSISTENT_ALPHA, 0L));
		publish();
	}

	public void flashTransient(String ruleId, ScreenFlashAction action)
	{
		long expiresAtMillis = System.currentTimeMillis() + (Math.max(1, action.getDurationTicks()) * 600L);
		transientFlashes.put(ruleId, new RuntimeOverlay.FlashEntry(parseColor(action.getColorHex()), TRANSIENT_ALPHA,
			expiresAtMillis));
		publish();
	}

	public void removePersistent(String ruleId)
	{
		persistentFlashes.remove(ruleId);
		publish();
	}

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
