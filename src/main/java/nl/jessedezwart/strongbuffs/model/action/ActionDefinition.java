package nl.jessedezwart.strongbuffs.model.action;

import nl.jessedezwart.strongbuffs.model.Definition;

/**
 * Base type for persisted action definitions.
 *
 * Implementations describe config editor fields, validation, and copy behavior
 * only. Runtime effects are delegated to handlers in
 * {@code nl.jessedezwart.strongbuffs.runtime.action}.
 *
 * Implementations should use transient fields for typeId, editorLabel and editorDescription, since these
 * can be derived from the class type and don't need to be persisted.
 */
public interface ActionDefinition extends Definition<ActionDefinition>
{
}
