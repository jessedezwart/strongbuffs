package nl.jessedezwart.strongbuffs.panel.editor;

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
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;
import nl.jessedezwart.strongbuffs.model.registry.DefinitionRegistry;

/**
 * Builds action editor metadata and input components from action model field definitions.
 */
@Singleton
public class ActionEditorRegistry
{
	private final List<Class<? extends ActionDefinition>> actionClasses;
	private final Map<Class<? extends ActionDefinition>, ActionDefinition> metadataByClass;

	public ActionEditorRegistry()
	{
		List<Class<? extends ActionDefinition>> items = loadActionClasses();
		actionClasses = Collections.unmodifiableList(items);

		Map<Class<? extends ActionDefinition>, ActionDefinition> byClass = new LinkedHashMap<>();

		for (Class<? extends ActionDefinition> actionClass : items)
		{
			byClass.put(actionClass, DefinitionRegistry.getActionMetadata(actionClass));
		}

		metadataByClass = Collections.unmodifiableMap(byClass);
	}

	public List<Class<? extends ActionDefinition>> getActionClasses()
	{
		return actionClasses;
	}

	public ActionDefinition getByActionClass(Class<? extends ActionDefinition> actionClass)
	{
		return metadataByClass.get(actionClass);
	}

	public ActionDefinition createDefaultAction(Class<? extends ActionDefinition> actionClass)
	{
		return DefinitionRegistry.createAction(actionClass);
	}

	public String describe(ActionDefinition actionDefinition)
	{
		if (actionDefinition == null)
		{
			return "No action";
		}

		return actionDefinition.getEditorDescription();
	}

	public JComponent createEditor(ActionDefinition actionDefinition, Runnable onChange)
	{
		JPanel panel = ActionEditorSupport.createVerticalPanel();

		for (EditorField field : actionDefinition.getEditorFields())
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

	private static List<Class<? extends ActionDefinition>> loadActionClasses()
	{
		List<Class<? extends ActionDefinition>> items =
			new ArrayList<>(RuleDefinitionStore.getSupportedActionDefinitionClasses());
		items.sort(Comparator.comparing(actionClass -> DefinitionRegistry.getActionMetadata(actionClass).getEditorLabel()));

		if (items.isEmpty())
		{
			throw new IllegalStateException("No action models were found.");
		}

		return items;
	}
}
