package nl.jessedezwart.strongbuffs.panel.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionLogic;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.panel.editor.ConditionEditorRegistry;
import org.junit.Test;

public class ConditionGroupPanelTest
{
	@Test
	public void addAndRemoveChildrenMutatesUnderlyingTree()
	{
		ConditionGroup root = new ConditionGroup();
		ConditionEditorRegistry conditionRegistry = new ConditionEditorRegistry();
		ConditionGroupPanel panel = new ConditionGroupPanel(root, true, conditionRegistry, () ->
		{
		}, () ->
		{
		}, null);

		panel.addCondition();
		panel.addGroup();

		assertEquals(2, root.getChildren().size());
		assertTrue(root.getChildren().get(0) instanceof ConditionDefinition);
		assertTrue(root.getChildren().get(1) instanceof ConditionGroup);

		panel.removeChild(root.getChildren().get(0));

		assertEquals(1, root.getChildren().size());
	}

	@Test
	public void nestedGroupKeepsEditableLogic()
	{
		ConditionGroup nested = new ConditionGroup();
		nested.setLogic(ConditionLogic.OR);

		assertEquals(ConditionLogic.OR, nested.getLogic());
		nested.setLogic(ConditionLogic.AND);
		assertEquals(ConditionLogic.AND, nested.getLogic());
	}
}
