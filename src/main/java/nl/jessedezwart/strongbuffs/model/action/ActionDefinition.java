package nl.jessedezwart.strongbuffs.model.action;

import java.util.List;
import java.util.Map;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;
import nl.jessedezwart.strongbuffs.panel.state.RuleDraft;

/**
 * Base type for persisted action definitions.
 *
 * Implementations describe config editor fields, validation, and copy behavior
 * only. Runtime effects are delegated to handlers in
 * {@code nl.jessedezwart.strongbuffs.runtime.action}.
 */
public abstract class ActionDefinition
{
	public abstract String getTypeId();

	/**
	 * @return a human-readable label for this action type, shown in the config
	 *         editor when listing available actions to add.
	 */
	public abstract String getEditorLabel();

	/**
	 * This can be used to provide more detailed information about the action, and
	 * is shown as a tooltip in the editor.
	 *
	 * @return a description of the action for the config editor
	 */
	public abstract String getEditorDescription();

	/**
	 * When copying a rule in the editor,
	 * {@link RuleDraft#copyAction(ActionDefinition)} gets called to create a copy
	 * of the action definition for the new rule. This is necessary to ensure that
	 * edits to one rule's action don't affect other rules that share the same
	 * action instance.
	 *
	 * @return a copy of this action definition
	 */
	public abstract ActionDefinition copy();

	/**
	 * THe config fields to show in the config editor. The fields are used to
	 * generate the editor UI and to collect user config input for this action.
	 *
	 * @return a list of editor fields
	 */
	public abstract List<EditorField> getEditorFields();

	/**
	 * Validates the action definition's config and adds any validation errors to
	 * the provided errors map, using fieldPrefix to namespace the error keys.
	 * Called by {@link RuleDraftValidator#validateAction()}
	 *
	 * @param errors
	 * @param fieldPrefix
	 */
	public abstract void validate(Map<String, String> errors, String fieldPrefix);

	/**
	 * Shared validation helper for actions that persist colors as HTML-style hex
	 * strings.
	 *
	 * @return true if the colorHex is a valid HTML hex color string, e.g. "#A1B2C3"
	 */
	protected final boolean isValidColorHex(String colorHex)
	{
		return colorHex != null && colorHex.matches("^#[0-9A-Fa-f]{6}$");
	}
}
