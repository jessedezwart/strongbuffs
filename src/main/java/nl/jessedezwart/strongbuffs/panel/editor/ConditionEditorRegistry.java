package nl.jessedezwart.strongbuffs.panel.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import nl.jessedezwart.strongbuffs.RuleDefinitionStore;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;
import nl.jessedezwart.strongbuffs.panel.state.RuleDescriptions;
import net.runelite.client.ui.ColorScheme;

/**
 * Builds condition editor metadata and field components from numeric condition model classes.
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
			byClass.put(conditionClass, instantiate(conditionClass));
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
		return instantiate(conditionClass);
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
		JPanel panel = new JPanel();
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JComboBox<ComparisonOperator> operatorComboBox = new JComboBox<>(ComparisonOperator.values());
		operatorComboBox.setSelectedItem(condition.getOperator());
		operatorComboBox.addActionListener(event ->
		{
			condition.setOperator((ComparisonOperator) operatorComboBox.getSelectedItem());
			onChange.run();
		});
		panel.add(operatorComboBox);

		JSpinner thresholdSpinner = new JSpinner(new SpinnerNumberModel(condition.getThreshold(),
			condition.getMinimumValue(), condition.getMaximumValue(), 1));
		thresholdSpinner.addChangeListener(event ->
		{
			condition.setThreshold((Integer) thresholdSpinner.getValue());
			onChange.run();
		});
		panel.add(thresholdSpinner);

		JLabel unitLabel = new JLabel(condition.getEditorUnit().trim());
		unitLabel.setForeground(ColorScheme.TEXT_COLOR);
		panel.add(unitLabel);
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

		items.sort(Comparator.comparing(conditionClass -> instantiate(conditionClass).getEditorLabel()));

		if (items.isEmpty())
		{
			throw new IllegalStateException("No numeric condition models were found.");
		}

		return items;
	}

	private static <T extends NumericConditionDefinition> T instantiate(Class<T> conditionClass)
	{
		try
		{
			return conditionClass.getDeclaredConstructor().newInstance();
		}
		catch (ReflectiveOperationException ex)
		{
			throw new IllegalStateException("Failed to create condition instance for " + conditionClass.getName(), ex);
		}
	}
}
