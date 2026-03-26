package nl.jessedezwart.strongbuffs.panel.state;

import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;

/**
 * Result of importing a single rule from JSON.
 */
public final class RuleImportResult
{
	private final RuleDefinition importedRule;
	private final String errorMessage;

	private RuleImportResult(RuleDefinition importedRule, String errorMessage)
	{
		this.importedRule = importedRule;
		this.errorMessage = errorMessage;
	}

	public static RuleImportResult success(RuleDefinition importedRule)
	{
		return new RuleImportResult(importedRule, null);
	}

	public static RuleImportResult failure(String errorMessage)
	{
		return new RuleImportResult(null, errorMessage);
	}

	public boolean isSuccess()
	{
		return importedRule != null;
	}

	public RuleDefinition getImportedRule()
	{
		return importedRule;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}
}
