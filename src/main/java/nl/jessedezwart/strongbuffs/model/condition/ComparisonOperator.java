package nl.jessedezwart.strongbuffs.model.condition;

/**
 * Supported numeric comparisons for threshold-based conditions.
 */
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

	public boolean matches(int actualValue, int thresholdValue)
	{
		switch (this)
		{
			case LESS_THAN:
				return actualValue < thresholdValue;
			case LESS_THAN_OR_EQUAL:
				return actualValue <= thresholdValue;
			case GREATER_THAN:
				return actualValue > thresholdValue;
			case GREATER_THAN_OR_EQUAL:
				return actualValue >= thresholdValue;
			default:
				return false;
		}
	}
}
