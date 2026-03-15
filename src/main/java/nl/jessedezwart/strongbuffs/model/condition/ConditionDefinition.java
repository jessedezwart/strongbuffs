package nl.jessedezwart.strongbuffs.model.condition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;

public abstract class ConditionDefinition implements ConditionNode
{
	public abstract String getEditorLabel();

	public abstract ConditionDefinition copy();

	public String getEditorDescription()
	{
		return getEditorLabel();
	}

	public List<EditorField> getEditorFields()
	{
		return Collections.emptyList();
	}

	public void validate(Map<String, String> errors, String fieldPrefix)
	{
	}

	protected final boolean isBlank(String value)
	{
		return value == null || value.trim().isEmpty();
	}
}
