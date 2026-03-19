package nl.jessedezwart.strongbuffs.panel.editor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import nl.jessedezwart.strongbuffs.model.EditorField;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;
import nl.jessedezwart.strongbuffs.model.registry.DefinitionCatalog;
import net.runelite.client.ui.ColorScheme;

/**
 * Builds condition editor metadata and Swing editors from persisted condition definitions.
 *
 * <p>The registry relies on the definition catalog for approved types and uses the editor metadata
 * exposed by each definition to render a generic form.</p>
 */
@Singleton
public class ConditionEditorRegistry
{
	private final List<Class<? extends ConditionDefinition>> conditionClasses;
	private final Map<Class<? extends ConditionDefinition>, ConditionDefinition> metadataByClass;
	private final DefinitionCatalog definitionCatalog;

	public ConditionEditorRegistry()
	{
		this(new DefinitionCatalog());
	}

	@Inject
	public ConditionEditorRegistry(DefinitionCatalog definitionCatalog)
	{
		this.definitionCatalog = definitionCatalog;
		List<Class<? extends ConditionDefinition>> items = loadConditionClasses(definitionCatalog);
		conditionClasses = Collections.unmodifiableList(items);

		Map<Class<? extends ConditionDefinition>, ConditionDefinition> byClass = new LinkedHashMap<>();

		for (Class<? extends ConditionDefinition> conditionClass : items)
		{
			byClass.put(conditionClass, definitionCatalog.getConditionMetadata(conditionClass));
		}

		metadataByClass = Collections.unmodifiableMap(byClass);
	}

	public List<Class<? extends ConditionDefinition>> getConditionClasses()
	{
		return conditionClasses;
	}

	public ConditionDefinition getByConditionClass(Class<? extends ConditionDefinition> conditionClass)
	{
		return metadataByClass.get(conditionClass);
	}

	public ConditionDefinition createDefaultCondition(Class<? extends ConditionDefinition> conditionClass)
	{
		return definitionCatalog.createCondition(conditionClass);
	}

	public ConditionDefinition copy(ConditionDefinition conditionDefinition)
	{
		return conditionDefinition.copy();
	}

	public String describe(ConditionDefinition conditionDefinition)
	{
		return conditionDefinition.getEditorDescription();
	}

	/**
	 * Creates an editor component bound directly to the provided draft condition instance.
	 */
	public JComponent createEditor(ConditionDefinition conditionDefinition, Runnable onChange)
	{
		if (conditionDefinition instanceof NumericConditionDefinition)
		{
			return createNumericEditor((NumericConditionDefinition) conditionDefinition, onChange);
		}

		JPanel panel = ActionEditorSupport.createVerticalPanel();
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		for (EditorField field : conditionDefinition.getEditorFields())
		{
			JComponent component = EditorFieldComponentFactory.createComponent(field, onChange);

			if (field instanceof EditorField.BooleanEditorField)
			{
				panel.add(component);
				continue;
			}

			panel.add(ActionEditorSupport.labeled(field.getLabel(), component));
		}

		return panel;
	}

	private static JComponent createNumericEditor(NumericConditionDefinition conditionDefinition, Runnable onChange)
	{
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setOpaque(true);
		panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

		for (EditorField field : conditionDefinition.getEditorFields())
		{
			JComponent component = EditorFieldComponentFactory.createComponent(field, onChange);

			if (component instanceof javax.swing.JComboBox)
			{
				component.setPreferredSize(new Dimension(88, component.getPreferredSize().height));
				component.setMaximumSize(component.getPreferredSize());
			}

			panel.add(component);
		}

		return panel;
	}

	private static List<Class<? extends ConditionDefinition>> loadConditionClasses(DefinitionCatalog definitionCatalog)
	{
		List<Class<? extends ConditionDefinition>> items =
			new ArrayList<>(definitionCatalog.getConditionDefinitions());

		items.sort(Comparator.comparing(conditionClass ->
			definitionCatalog.getConditionMetadata(conditionClass).getEditorLabel()));

		if (items.isEmpty())
		{
			throw new IllegalStateException("No condition models were found.");
		}

		return items;
	}
}
