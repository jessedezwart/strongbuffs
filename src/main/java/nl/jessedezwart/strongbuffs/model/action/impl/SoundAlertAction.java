package nl.jessedezwart.strongbuffs.model.action.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SoundAlertAction extends ActionDefinition
{
	private static final Map<String, String> SOUND_LABELS_BY_KEY = createSoundLabelsByKey();

	private String soundKey = "notification";
	private int volumePercent = 100;

	@Override
	public String getTypeId()
	{
		return "sound_alert";
	}

	@Override
	public String getEditorLabel()
	{
		return "Sound alert";
	}

	@Override
	public String getEditorDescription()
	{
		return "Sound: " + soundKey;
	}

	@Override
	public ActionDefinition copy()
	{
		SoundAlertAction copy = new SoundAlertAction();
		copy.setSoundKey(soundKey);
		copy.setVolumePercent(volumePercent);
		return copy;
	}

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (soundKey == null || soundKey.trim().isEmpty())
		{
			errors.put(fieldPrefix + ".soundKey", "Choose a sound preset.");
		}

		if (volumePercent < 0 || volumePercent > 100)
		{
			errors.put(fieldPrefix + ".volumePercent", "Volume must be between 0 and 100.");
		}
	}

	public static Map<String, String> getSoundLabelsByKey()
	{
		return SOUND_LABELS_BY_KEY;
	}

	public static String getSoundLabel(String soundKey)
	{
		return SOUND_LABELS_BY_KEY.getOrDefault(soundKey, soundKey);
	}

	private static Map<String, String> createSoundLabelsByKey()
	{
		Map<String, String> sounds = new LinkedHashMap<>();
		sounds.put("notification", "Notification");
		sounds.put("ding", "Ding");
		sounds.put("warning", "Warning");
		return Collections.unmodifiableMap(sounds);
	}
}
