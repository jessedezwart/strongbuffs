package nl.jessedezwart.strongbuffs.model.condition.tree;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A persisted AND/OR branch containing nested groups and condition leaves.
 */
@Data
@NoArgsConstructor
public class ConditionGroup implements ConditionNode
{
	private ConditionLogic logic = ConditionLogic.AND;
	private List<ConditionNode> children = new ArrayList<>();

	@Override
	public String getTypeId()
	{
		return "group";
	}
}
