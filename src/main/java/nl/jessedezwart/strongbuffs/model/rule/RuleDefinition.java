package nl.jessedezwart.strongbuffs.model.rule;

import lombok.Data;
import lombok.NoArgsConstructor;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;

/**
 * Persisted user-defined rule configuration.
 *
 * <p>This is the handoff object between the editor, persistence store, and runtime compiler. The
 * runtime never mutates these definitions directly; it compiles them into dedicated runtime
 * structures instead.</p>
 */
@Data
@NoArgsConstructor
public class RuleDefinition
{
	// Stored with each saved rule so deserialization can reject or migrate older shapes safely.
	private int schemaVersion = 1;
	private String id;
	private String name;
	private boolean enabled = true;
	private ConditionGroup rootGroup = new ConditionGroup();
	private ActivationMode activationMode = ActivationMode.WHILE_ACTIVE;
	private int cooldownTicks;
	private ActionDefinition action;
}
