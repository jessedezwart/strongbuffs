package nl.jessedezwart.strongbuffs.model.action.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
/**
 * Persisted action definition for flashing the screen with a configured color.
 */
public class ScreenFlashAction extends ActionDefinition
{
	private String colorHex = "#FF0000";
	private int durationTicks = 1;

	@Override
	public String getTypeId()
	{
		return "screen_flash";
	}

	@Override
	public String getEditorLabel()
	{
		return "Screen flash";
	}

	@Override
	public String getEditorDescription()
	{
		return "Screen flash";
	}

	@Override
	public ActionDefinition copy()
	{
		ScreenFlashAction copy = new ScreenFlashAction();
		copy.setColorHex(colorHex);
		copy.setDurationTicks(durationTicks);
		return copy;
	}

	@Override
	public List<EditorField> getEditorFields()
	{
		return Arrays.asList(
			EditorField.color("colorHex", "Color", 8, this::getColorHex, this::setColorHex),
			EditorField.slider("durationTicks", "Duration", this::getDurationTicks, this::setDurationTicks, 1, 10, 1,
				true, true));
	}

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (!isValidColorHex(colorHex))
		{
			errors.put(fieldPrefix + ".color", "Color must be in #RRGGBB format.");
		}

		if (durationTicks < 1 || durationTicks > 10)
		{
			errors.put(fieldPrefix + ".durationTicks", "Duration must be between 1 and 10 ticks.");
		}
	}
}
