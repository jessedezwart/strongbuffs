package nl.jessedezwart.strongbuffs.model.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SpecCondition extends ConditionDefinition
{
	private ComparisonOperator operator = ComparisonOperator.GREATER_THAN_OR_EQUAL;
	private int threshold;
}
