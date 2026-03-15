package nl.jessedezwart.strongbuffs.model.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class HpCondition extends ConditionDefinition
{
	private ComparisonOperator operator = ComparisonOperator.LESS_THAN_OR_EQUAL;
	private int threshold;
}
