package nl.jessedezwart.strongbuffs.model.condition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;

/**
 * Base type for persisted leaf conditions.
 *
 * <p>Condition definitions never read live RuneLite state directly. They only store user intent and
 * editor metadata so the runtime layer can evaluate them against cached state later.</p>
 */
public abstract class ConditionDefinition implements ConditionNode
{
	public abstract String getEditorLabel();

	/**
	 * Creates a detached copy for draft editing and persistence workflows.
	 */
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

	/**
	 * Shared utility for user-entered text fields that should reject blank values after trimming.
	 */
	protected final boolean isBlank(String value)
	{
		return value == null || value.trim().isEmpty();
	}
}
