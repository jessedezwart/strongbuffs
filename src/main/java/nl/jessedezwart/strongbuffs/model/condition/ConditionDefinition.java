package nl.jessedezwart.strongbuffs.model.condition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.jessedezwart.strongbuffs.model.Definition;
import nl.jessedezwart.strongbuffs.model.EditorField;

/**
 * Base type for persisted leaf conditions.
 *
 * Implementations describe config editor fields, validation, and copy behavior
 * only. Runtime effects are delegated to handlers in
 * {@code nl.jessedezwart.strongbuffs.runtime.condition}.
 */
public abstract class ConditionDefinition implements ConditionNode, Definition<ConditionDefinition>
{
	/**
	 * Creates a detached copy for draft editing and persistence workflows.
	 */
	public abstract ConditionDefinition copy();

	/**
	 * @return String Description for this condition type, shown in the editor as a
	 *         tooltip. By default, this returns the same value as
	 *         {@link #getEditorLabel()}, but implementations can override this to
	 *         provide more detailed descriptions if needed.
	 */
	@Override
	public String getEditorDescription()
	{
		return getEditorLabel();
	}

	/**
	 * @return List<EditorField> List of editor fields for this condition's config.
	 *         By default, this returns an empty list, but implementations can
	 *         override this to provide custom fields as needed.
	 */
	@Override
	public List<EditorField> getEditorFields()
	{
		return Collections.emptyList();
	}

	/**
	 * A no-op validate method. Implementations can override this to add validation
	 * logic for their config fields.
	 */
	@Override
	public void validate(Map<String, String> errors, String fieldPrefix)
	{
	}

	/**
	 * @param value String to check for null or blank
	 * @return boolean true if the value is null or blank, false otherwise
	 */
	protected final boolean isBlank(String value)
	{
		return value == null || value.trim().isEmpty();
	}
}
