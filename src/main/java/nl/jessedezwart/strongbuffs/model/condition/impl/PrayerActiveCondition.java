package nl.jessedezwart.strongbuffs.model.condition.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.api.Prayer;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionEditorOptions;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;

@Data
@EqualsAndHashCode(callSuper = false)
/**
 * Persisted condition definition that checks whether a specific prayer is active.
 */
public class PrayerActiveCondition extends ConditionDefinition
{
	private Prayer prayer = Prayer.THICK_SKIN;
	private boolean active = true;

	@Override
	public String getEditorLabel()
	{
		return "Prayer active";
	}

	@Override
	public String getEditorDescription()
	{
		return "Prayer " + formatPrayer(prayer) + " is " + (active ? "active" : "inactive");
	}

	@Override
	public ConditionDefinition copy()
	{
		PrayerActiveCondition copy = new PrayerActiveCondition();
		copy.setPrayer(prayer);
		copy.setActive(active);
		return copy;
	}

	@Override
	public List<EditorField> getEditorFields()
	{
		return Arrays.asList(
			EditorField.choice("prayer", "Prayer", this::getPrayer, this::setPrayer, ConditionEditorOptions.getPrayers(),
				PrayerActiveCondition::formatPrayer),
			EditorField.checkbox("active", "Active", this::isActive, this::setActive));
	}

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (prayer == null)
		{
			errors.put(fieldPrefix, "Choose a prayer.");
		}
	}

	@Override
	public String getTypeId()
	{
		return "prayer_active";
	}

	private static String formatPrayer(Prayer prayer)
	{
		if (prayer == null)
		{
			return "";
		}

		String normalized = prayer.name().toLowerCase().replace('_', ' ');
		String[] words = normalized.split(" ");
		StringBuilder builder = new StringBuilder();

		for (String word : words)
		{
			if (builder.length() > 0)
			{
				builder.append(' ');
			}

			builder.append(Character.toUpperCase(word.charAt(0)));
			builder.append(word.substring(1));
		}

		return builder.toString();
	}
}
