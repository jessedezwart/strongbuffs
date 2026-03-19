package nl.jessedezwart.strongbuffs.model;

import java.util.List;

/**
 * A type that can be displayed and edited in the config editor panel.
 */
public interface Editable
{
	/**
	 * @return a human-readable label shown in the editor
	 */
	String getEditorLabel();

	/**
	 * @return a description shown as a tooltip in the editor
	 */
	String getEditorDescription();

	/**
	 * @return the config fields to render in the editor UI
	 */
	List<EditorField> getEditorFields();
}
