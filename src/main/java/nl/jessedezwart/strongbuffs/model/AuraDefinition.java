package nl.jessedezwart.strongbuffs.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import nl.jessedezwart.strongbuffs.model.display.DisplayDefinition;

@Data
@NoArgsConstructor
public class AuraDefinition
{
	private int schemaVersion = 1;
	private String id;
	private String name;
	private boolean enabled = true;
	private ConditionGroup rootGroup = new ConditionGroup();
	private ActivationMode activationMode = ActivationMode.WHILE_ACTIVE;
	private int cooldownTicks;
	private DisplayDefinition display;
}
