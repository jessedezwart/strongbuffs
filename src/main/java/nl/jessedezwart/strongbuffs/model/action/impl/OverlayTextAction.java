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
import nl.jessedezwart.strongbuffs.model.util.ColorUtil;

/**
 * Persisted action definition for showing text in the shared runtime overlay.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class OverlayTextAction implements ActionDefinition
{
	private final transient String typeId = "overlay_text";
	private final transient String editorLabel = "Overlay text";
	private final transient String editorDescription = "Overlay text";

	private String text;
	private String colorHex = "#FFFFFF";
	private Boolean showValue = true;

	@Override
	public ActionDefinition copy()
	{
		OverlayTextAction copy = new OverlayTextAction();
		copy.setText(text);
		copy.setColorHex(colorHex);
		copy.setShowValue(showValue);
		return copy;
	}

	@Override
	public List<EditorField> getEditorFields()
	{
		return Arrays.asList(
			EditorField.text("text", "Text", 14, this::getText, this::setText),
			EditorField.color("colorHex", "Color", 8, this::getColorHex, this::setColorHex),
			EditorField.checkbox("showValue", "Show live value", this::getShowValue, this::setShowValue));
	}

	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (text == null || text.trim().isEmpty())
		{
			errors.put(fieldPrefix + ".text", "Text is required.");
		}

		if (!ColorUtil.isValidColorHex(colorHex))
		{
			errors.put(fieldPrefix + ".color", "Color must be in #RRGGBB format.");
		}
	}
}
