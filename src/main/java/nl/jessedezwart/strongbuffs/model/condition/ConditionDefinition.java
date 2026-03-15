package nl.jessedezwart.strongbuffs.model.condition;

import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;

public abstract class ConditionDefinition implements ConditionNode
{
	public abstract ConditionDefinition copy();
}
