package nl.jessedezwart.strongbuffs.model.action.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.jessedezwart.strongbuffs.model.EditorField;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.util.ColorUtil;

/**
 * Persisted action definition for flashing the screen with a configured color.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ScreenFlashAction implements ActionDefinition
{
	private final transient String typeId = "screen_flash";
	private final transient String editorLabel = "Screen flash";
	private final transient String editorDescription = "Screen flash";

	private String colorHex = "#FF0000";
	private Integer durationTicks = 1;

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
		if (!ColorUtil.isValidColorHex(colorHex))
		{
			errors.put(fieldPrefix + ".color", "Color must be in #RRGGBB format.");
		}

		if (durationTicks < 1 || durationTicks > 10)
		{
			errors.put(fieldPrefix + ".durationTicks", "Duration must be between 1 and 10 ticks.");
		}
	}
}
