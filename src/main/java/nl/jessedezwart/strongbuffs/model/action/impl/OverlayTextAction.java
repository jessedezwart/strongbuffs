package nl.jessedezwart.strongbuffs.model.action.impl;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OverlayTextAction extends ActionDefinition
{
	private String text;
	private String colorHex = "#FFFFFF";
	private boolean showValue = true;

	@Override
	public String getTypeId()
	{
		return "overlay_text";
	}

	@Override
	public String getEditorLabel()
	{
		return "Overlay text";
	}

	@Override
	public String getEditorDescription()
	{
		return "Overlay text";
	}

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
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
		if (text == null || text.trim().isEmpty())
		{
			errors.put(fieldPrefix + ".text", "Text is required.");
		}

		if (!isValidColorHex(colorHex))
		{
			errors.put(fieldPrefix + ".color", "Color must be in #RRGGBB format.");
		}
	}
}
