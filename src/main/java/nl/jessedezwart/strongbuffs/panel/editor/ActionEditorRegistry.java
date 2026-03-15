package nl.jessedezwart.strongbuffs.panel.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JTextField;
import net.runelite.client.ui.ColorScheme;
import nl.jessedezwart.strongbuffs.RuleDefinitionStore;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.action.impl.ScreenFlashAction;
import nl.jessedezwart.strongbuffs.model.action.impl.SoundAlertAction;

/**
 * Builds action editor metadata and field components directly from approved action model classes.
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
			byClass.put(actionClass, instantiate(actionClass));
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
		return instantiate(actionClass);
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
		if (actionDefinition instanceof OverlayTextAction)
		{
			return createOverlayTextEditor((OverlayTextAction) actionDefinition, onChange);
		}

		if (actionDefinition instanceof ScreenFlashAction)
		{
			return createScreenFlashEditor((ScreenFlashAction) actionDefinition, onChange);
		}

		if (actionDefinition instanceof SoundAlertAction)
		{
			return createSoundAlertEditor((SoundAlertAction) actionDefinition, onChange);
		}

		throw new IllegalArgumentException("Unsupported action type: " + actionDefinition.getClass().getName());
	}

	private static List<Class<? extends ActionDefinition>> loadActionClasses()
	{
		List<Class<? extends ActionDefinition>> items =
			new ArrayList<>(RuleDefinitionStore.getSupportedActionDefinitionClasses());
		items.sort(Comparator.comparing(actionClass -> instantiate(actionClass).getEditorLabel()));

		if (items.isEmpty())
		{
			throw new IllegalStateException("No action models were found.");
		}

		return items;
	}

	private static JComponent createOverlayTextEditor(OverlayTextAction action, Runnable onChange)
	{
		javax.swing.JPanel panel = ActionEditorSupport.createVerticalPanel();

		JTextField textField = new JTextField(action.getText() == null ? "" : action.getText(), 14);
		textField.getDocument().addDocumentListener(ActionEditorSupport.documentListener(() ->
		{
			action.setText(textField.getText());
			onChange.run();
		}));
		panel.add(ActionEditorSupport.labeled("Text", textField));

		JTextField colorField = new JTextField(action.getColorHex(), 8);
		colorField.getDocument().addDocumentListener(ActionEditorSupport.documentListener(() ->
		{
			action.setColorHex(colorField.getText());
			onChange.run();
		}));
		panel.add(ActionEditorSupport.labeled("Color", colorField));

		JCheckBox showValueCheckBox = new JCheckBox("Show live value", action.isShowValue());
		showValueCheckBox.setBackground(ColorScheme.DARK_GRAY_COLOR);
		showValueCheckBox.setForeground(ColorScheme.TEXT_COLOR);
		showValueCheckBox.addActionListener(event ->
		{
			action.setShowValue(showValueCheckBox.isSelected());
			onChange.run();
		});
		panel.add(showValueCheckBox);
		return panel;
	}

	private static JComponent createScreenFlashEditor(ScreenFlashAction action, Runnable onChange)
	{
		javax.swing.JPanel panel = ActionEditorSupport.createVerticalPanel();

		JTextField colorField = new JTextField(action.getColorHex(), 8);
		colorField.getDocument().addDocumentListener(ActionEditorSupport.documentListener(() ->
		{
			action.setColorHex(colorField.getText());
			onChange.run();
		}));
		panel.add(ActionEditorSupport.labeled("Color", colorField));

		JSlider durationSlider = new JSlider(1, 10, action.getDurationTicks());
		durationSlider.setBackground(ColorScheme.DARK_GRAY_COLOR);
		durationSlider.setForeground(ColorScheme.TEXT_COLOR);
		durationSlider.setMajorTickSpacing(1);
		durationSlider.setPaintTicks(true);
		durationSlider.setPaintLabels(true);
		durationSlider.addChangeListener(event ->
		{
			action.setDurationTicks(durationSlider.getValue());
			onChange.run();
		});
		panel.add(ActionEditorSupport.labeled("Duration", durationSlider));
		return panel;
	}

	private static JComponent createSoundAlertEditor(SoundAlertAction action, Runnable onChange)
	{
		javax.swing.JPanel panel = ActionEditorSupport.createVerticalPanel();

		List<String> soundKeys = new ArrayList<>(SoundAlertAction.getSoundLabelsByKey().keySet());
		JComboBox<String> soundComboBox = new JComboBox<>(soundKeys.toArray(new String[0]));
		soundComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) ->
			new javax.swing.DefaultListCellRenderer()
				.getListCellRendererComponent(list,
					value == null ? "" : SoundAlertAction.getSoundLabel((String) value),
					index, isSelected, cellHasFocus));
		soundComboBox.setSelectedItem(action.getSoundKey());
		soundComboBox.addActionListener(event ->
		{
			action.setSoundKey((String) soundComboBox.getSelectedItem());
			onChange.run();
		});
		panel.add(ActionEditorSupport.labeled("Preset", soundComboBox));

		JSlider volumeSlider = new JSlider(0, 100, action.getVolumePercent());
		volumeSlider.setBackground(ColorScheme.DARK_GRAY_COLOR);
		volumeSlider.setForeground(ColorScheme.TEXT_COLOR);
		volumeSlider.setMajorTickSpacing(25);
		volumeSlider.setPaintTicks(true);
		volumeSlider.setPaintLabels(true);
		volumeSlider.addChangeListener(event ->
		{
			action.setVolumePercent(volumeSlider.getValue());
			onChange.run();
		});
		panel.add(ActionEditorSupport.labeled("Volume", volumeSlider));
		return panel;
	}

	private static <T extends ActionDefinition> T instantiate(Class<T> actionClass)
	{
		try
		{
			return actionClass.getDeclaredConstructor().newInstance();
		}
		catch (ReflectiveOperationException ex)
		{
			throw new IllegalStateException("Failed to create action instance for " + actionClass.getName(), ex);
		}
	}
}
