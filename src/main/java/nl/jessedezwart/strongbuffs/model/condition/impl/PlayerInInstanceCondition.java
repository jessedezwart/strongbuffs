package nl.jessedezwart.strongbuffs.model.condition.impl;

import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
/**
 * Persisted condition definition that checks whether the local player is inside an instance.
 */
public class PlayerInInstanceCondition extends ConditionDefinition
{
	@Override
	public String getEditorLabel()
	{
		return "Player in instance";
	}

	@Override
	public String getEditorDescription()
	{
		return "Inside an instance";
	}

	@Override
	public ConditionDefinition copy()
	{
		return new PlayerInInstanceCondition();
	}

	@Override
	public String getTypeId()
	{
		return "player_in_instance";
	}
}
