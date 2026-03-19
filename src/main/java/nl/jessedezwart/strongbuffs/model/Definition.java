package nl.jessedezwart.strongbuffs.model;

import java.util.Map;

/**
 * A definition that can be copied for draft editing and validated before
 * persistence. The runtime logic for checking definitions is delegated to
 * handlers in the runtime package.
 * 
 * @code {@link nl.jessedezwart.strongbuffs.runtime}
 *
 *       @param <T> the concrete definition type, so {@link #copy()} returns the
 *       correct type
 */
public interface Definition<T extends Definition<T>> extends Editable
{
	/**
	 * @return stable string identifier used as the JSON type discriminator during serialization
	 */
	String getTypeId();

	/**
	 * @return a deep copy of this definition for draft editing
	 */
	T copy();

	/**
	 * Validates this definition's config and adds any errors to the provided map.
	 *
	 * @param errors      map to collect validation errors into
	 * @param fieldPrefix prefix for namespacing error keys
	 */
	void validate(Map<String, String> errors, String fieldPrefix);
}
