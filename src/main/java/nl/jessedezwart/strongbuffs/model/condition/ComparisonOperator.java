package nl.jessedezwart.strongbuffs.model.condition;

public enum ComparisonOperator
{
	LESS_THAN("<"),
	LESS_THAN_OR_EQUAL("<="),
	GREATER_THAN(">"),
	GREATER_THAN_OR_EQUAL(">=");

	private final String editorLabel;

	ComparisonOperator(String editorLabel)
	{
		this.editorLabel = editorLabel;
	}

	public String getEditorLabel()
	{
		return editorLabel;
	}
}
