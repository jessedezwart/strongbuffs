package nl.jessedezwart.strongbuffs.panel.state;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable validation result keyed by logical form field identifiers.
 */
public final class RuleValidationResult
{
	private static final RuleValidationResult VALID = new RuleValidationResult(Collections.emptyMap());

	private final Map<String, String> fieldErrors;

	private RuleValidationResult(Map<String, String> fieldErrors)
	{
		this.fieldErrors = fieldErrors;
	}

	public static RuleValidationResult valid()
	{
		return VALID;
	}

	public static RuleValidationResult of(Map<String, String> fieldErrors)
	{
		if (fieldErrors == null || fieldErrors.isEmpty())
		{
			return valid();
		}

		return new RuleValidationResult(Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors)));
	}

	public boolean isValid()
	{
		return fieldErrors.isEmpty();
	}

	public String getFieldError(String fieldKey)
	{
		return fieldErrors.get(fieldKey);
	}

	public Map<String, String> getFieldErrors()
	{
		return fieldErrors;
	}
}
