package nl.jessedezwart.strongbuffs.model.condition;

import java.util.Collections;
import java.util.List;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;

public abstract class ConditionDefinition implements ConditionNode
{
	public abstract ConditionDefinition copy();

	public List<EditorField> getEditorFields()
	{
		return Collections.emptyList();
	}
}
