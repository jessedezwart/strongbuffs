package nl.jessedezwart.strongbuffs.model.condition.tree;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persisted branch node for nested AND/OR condition logic.
 *
 * <p>Groups make the rule tree recursive while keeping individual condition definitions focused on
 * one check each.</p>
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
