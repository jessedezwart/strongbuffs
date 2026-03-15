package nl.jessedezwart.strongbuffs.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConditionGroup implements ConditionNode
{
	private ConditionLogic logic = ConditionLogic.AND;
	private List<ConditionNode> children = new ArrayList<>();
}
