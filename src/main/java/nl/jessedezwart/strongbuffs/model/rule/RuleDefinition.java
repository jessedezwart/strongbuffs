package nl.jessedezwart.strongbuffs.model.rule;

import lombok.Data;
import lombok.NoArgsConstructor;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;

/**
 * Persisted user-defined rule configuration.
 */
@Data
@NoArgsConstructor
public class RuleDefinition
{
	// Used for migration purposes, should be incremented when breaking changes are made to the schema.
	private int schemaVersion = 1;
	private String id;
	private String name;
	private boolean enabled = true;
	private ConditionGroup rootGroup = new ConditionGroup();
	private ActivationMode activationMode = ActivationMode.WHILE_ACTIVE;
	private int cooldownTicks;
	private ActionDefinition action;
}
