package nl.jessedezwart.strongbuffs.panel.state;

import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.RuleDefinitionValidator;
import nl.jessedezwart.strongbuffs.RuleJsonCodec;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;

/**
 * Parses and validates single-rule JSON imports from the external rule builder.
 */
@Singleton
public class RuleImportService
{
	private final RuleJsonCodec ruleJsonCodec;
	private final RuleDefinitionValidator ruleDefinitionValidator;

	@Inject
	public RuleImportService(RuleJsonCodec ruleJsonCodec, RuleDefinitionValidator ruleDefinitionValidator)
	{
		this.ruleJsonCodec = ruleJsonCodec;
		this.ruleDefinitionValidator = ruleDefinitionValidator;
	}

	public RuleImportResult importRule(String serializedRule)
	{
		RuleDefinition importedRule;

		try
		{
			importedRule = ruleJsonCodec.deserializeRule(serializedRule);
		}
		catch (IllegalArgumentException ex)
		{
			return RuleImportResult.failure(ex.getMessage());
		}

		importedRule.setId(UUID.randomUUID().toString());
		importedRule.setAction(copyAction(importedRule.getAction()));
		RuleValidationResult validationResult = ruleDefinitionValidator.validate(importedRule);

		if (!validationResult.isValid())
		{
			return RuleImportResult.failure(validationResult.getFieldErrors().values().iterator().next());
		}

		if (importedRule.getName() != null)
		{
			importedRule.setName(importedRule.getName().trim());
		}

		return RuleImportResult.success(importedRule);
	}

	private static ActionDefinition copyAction(ActionDefinition actionDefinition)
	{
		return actionDefinition == null ? null : actionDefinition.copy();
	}
}
