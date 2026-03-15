package nl.jessedezwart.strongbuffs.panel.editor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import net.runelite.client.ui.ColorScheme;
import nl.jessedezwart.strongbuffs.model.editor.EditorField;

final class EditorFieldComponentFactory
{
	private EditorFieldComponentFactory()
	{
	}

	static JComponent createComponent(EditorField field, Runnable onChange)
	{
		if (field instanceof EditorField.TextEditorField)
		{
			return createTextField((EditorField.TextEditorField) field, onChange);
		}

		if (field instanceof EditorField.BooleanEditorField)
		{
			return createBooleanField((EditorField.BooleanEditorField) field, onChange);
		}

		if (field instanceof EditorField.IntegerSliderEditorField)
		{
			return createSliderField((EditorField.IntegerSliderEditorField) field, onChange);
		}

		if (field instanceof EditorField.IntegerSpinnerEditorField)
		{
			return createSpinnerField((EditorField.IntegerSpinnerEditorField) field, onChange);
		}

		if (field instanceof EditorField.ChoiceEditorField)
		{
			return createChoiceField((EditorField.ChoiceEditorField<?>) field, onChange);
		}

		throw new IllegalArgumentException("Unsupported editor field: " + field.getClass().getName());
	}

	private static JTextField createTextField(EditorField.TextEditorField field, Runnable onChange)
	{
		JTextField textField = new JTextField(valueOrEmpty(field.getGetter().get()), field.getColumns());
		textField.getDocument().addDocumentListener(ActionEditorSupport.documentListener(() ->
		{
			field.getSetter().accept(textField.getText());
			onChange.run();
		}));
		return textField;
	}

	private static JCheckBox createBooleanField(EditorField.BooleanEditorField field, Runnable onChange)
	{
		JCheckBox checkBox = new JCheckBox(field.getLabel(), Boolean.TRUE.equals(field.getGetter().get()));
		checkBox.setBackground(ColorScheme.DARK_GRAY_COLOR);
		checkBox.setForeground(ColorScheme.TEXT_COLOR);
		checkBox.addActionListener(event ->
		{
			field.getSetter().accept(checkBox.isSelected());
			onChange.run();
		});
		return checkBox;
	}

	private static JSlider createSliderField(EditorField.IntegerSliderEditorField field, Runnable onChange)
	{
		JSlider slider = new JSlider(field.getMinimumValue(), field.getMaximumValue(), field.getGetter().get());
		slider.setBackground(ColorScheme.DARK_GRAY_COLOR);
		slider.setForeground(ColorScheme.TEXT_COLOR);
		slider.setMajorTickSpacing(field.getMajorTickSpacing());
		slider.setPaintTicks(field.isPaintTicks());
		slider.setPaintLabels(field.isPaintLabels());
		slider.addChangeListener(event ->
		{
			field.getSetter().accept(slider.getValue());
			onChange.run();
		});
		return slider;
	}

	private static JComponent createSpinnerField(EditorField.IntegerSpinnerEditorField field, Runnable onChange)
	{
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(field.getGetter().get().intValue(), field.getMinimumValue(),
			field.getMaximumValue(), field.getStepSize()));
		spinner.setPreferredSize(new Dimension(48, spinner.getPreferredSize().height));
		spinner.setMaximumSize(spinner.getPreferredSize());
		spinner.addChangeListener(event ->
		{
			field.getSetter().accept((Integer) spinner.getValue());
			onChange.run();
		});

		if (field.getSuffix() == null || field.getSuffix().isEmpty())
		{
			return spinner;
		}

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.add(spinner);

		JLabel suffixLabel = new JLabel(field.getSuffix());
		suffixLabel.setForeground(ColorScheme.TEXT_COLOR);
		panel.add(suffixLabel);
		return panel;
	}

	private static <T> JComboBox<T> createChoiceField(EditorField.ChoiceEditorField<T> field, Runnable onChange)
	{
		JComboBox<T> comboBox = new JComboBox<>(new Vector<>(field.getOptions()));
		comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) ->
			new javax.swing.DefaultListCellRenderer().getListCellRendererComponent(list,
				value == null ? "" : field.getOptionLabeler().apply(value), index, isSelected, cellHasFocus));
		comboBox.setSelectedItem(field.getGetter().get());
		comboBox.addActionListener(event ->
		{
			int selectedIndex = comboBox.getSelectedIndex();

			if (selectedIndex < 0)
			{
				return;
			}

			field.getSetter().accept(comboBox.getItemAt(selectedIndex));
			onChange.run();
		});
		return comboBox;
	}

	private static String valueOrEmpty(String value)
	{
		return value == null ? "" : value;
	}
}
