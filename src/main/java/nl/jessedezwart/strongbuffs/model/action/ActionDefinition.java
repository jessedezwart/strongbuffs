package nl.jessedezwart.strongbuffs.model.action;

import java.util.List;
import java.util.Map;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;

public abstract class ActionDefinition
{
	public abstract String getTypeId();

	public abstract String getEditorLabel();

	public abstract String getEditorDescription();

	public abstract ActionDefinition copy();

	public abstract List<EditorField> getEditorFields();

	public abstract void validate(Map<String, String> errors, String fieldPrefix);

	protected final boolean isValidColorHex(String colorHex)
	{
		return colorHex != null && colorHex.matches("^#[0-9A-Fa-f]{6}$");
	}
}
