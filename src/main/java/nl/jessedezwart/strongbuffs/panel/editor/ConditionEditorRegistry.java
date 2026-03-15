package nl.jessedezwart.strongbuffs.panel.editor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import nl.jessedezwart.strongbuffs.RuleDefinitionStore;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;
import nl.jessedezwart.strongbuffs.model.registry.DefinitionRegistry;
import nl.jessedezwart.strongbuffs.panel.state.RuleDescriptions;
import net.runelite.client.ui.ColorScheme;

/**
 * Builds condition editor metadata and field components from numeric condition model definitions.
 */
@Singleton
public class ConditionEditorRegistry
{
	private final List<Class<? extends NumericConditionDefinition>> conditionClasses;
	private final Map<Class<? extends ConditionDefinition>, NumericConditionDefinition> metadataByClass;

	public ConditionEditorRegistry()
	{
		List<Class<? extends NumericConditionDefinition>> items = loadConditionClasses();
		conditionClasses = Collections.unmodifiableList(items);

		Map<Class<? extends ConditionDefinition>, NumericConditionDefinition> byClass = new LinkedHashMap<>();

		for (Class<? extends NumericConditionDefinition> conditionClass : items)
		{
			byClass.put(conditionClass, (NumericConditionDefinition) DefinitionRegistry.getConditionMetadata(conditionClass));
		}

		metadataByClass = Collections.unmodifiableMap(byClass);
	}

	public List<Class<? extends NumericConditionDefinition>> getConditionClasses()
	{
		return conditionClasses;
	}

	public NumericConditionDefinition getByConditionClass(Class<? extends ConditionDefinition> conditionClass)
	{
		return metadataByClass.get(conditionClass);
	}

	public NumericConditionDefinition createDefaultCondition(Class<? extends NumericConditionDefinition> conditionClass)
	{
		return DefinitionRegistry.createCondition(conditionClass);
	}

	public ConditionDefinition copy(ConditionDefinition conditionDefinition)
	{
		if (!(conditionDefinition instanceof NumericConditionDefinition))
		{
			throw new IllegalArgumentException("Unsupported condition type: " + conditionDefinition.getClass().getName());
		}

		return conditionDefinition.copy();
	}

	public String describe(ConditionDefinition conditionDefinition)
	{
		if (!(conditionDefinition instanceof NumericConditionDefinition))
		{
			return conditionDefinition.getClass().getSimpleName();
		}

		NumericConditionDefinition condition = (NumericConditionDefinition) conditionDefinition;
		return condition.getEditorLabel() + " " + RuleDescriptions.describeComparisonOperator(condition.getOperator()) +
			" " + condition.getThreshold() + condition.getEditorUnit();
	}

	public JComponent createEditor(ConditionDefinition conditionDefinition, Runnable onChange)
	{
		if (!(conditionDefinition instanceof NumericConditionDefinition))
		{
			throw new IllegalArgumentException("Unsupported condition type: " + conditionDefinition.getClass().getName());
		}

		NumericConditionDefinition condition = (NumericConditionDefinition) conditionDefinition;
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setOpaque(true);
		panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

		for (EditorField field : condition.getEditorFields())
		{
			JComponent component = EditorFieldComponentFactory.createComponent(field, onChange);

			if (component instanceof javax.swing.JComboBox)
			{
				component.setPreferredSize(new Dimension(56, component.getPreferredSize().height));
				component.setMaximumSize(component.getPreferredSize());
			}

			panel.add(component);
		}

		return panel;
	}

	private static List<Class<? extends NumericConditionDefinition>> loadConditionClasses()
	{
		List<Class<? extends NumericConditionDefinition>> items = new ArrayList<>();

		for (Class<? extends ConditionDefinition> conditionClass : RuleDefinitionStore.getSupportedConditionDefinitionClasses())
		{
			if (NumericConditionDefinition.class.isAssignableFrom(conditionClass))
			{
				items.add(conditionClass.asSubclass(NumericConditionDefinition.class));
			}
		}

		items.sort(Comparator.comparing(conditionClass ->
			((NumericConditionDefinition) DefinitionRegistry.getConditionMetadata(conditionClass)).getEditorLabel()));

		if (items.isEmpty())
		{
			throw new IllegalStateException("No numeric condition models were found.");
		}

		return items;
	}
}
