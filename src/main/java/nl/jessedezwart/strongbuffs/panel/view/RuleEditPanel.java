package nl.jessedezwart.strongbuffs.panel.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.rule.ActivationMode;
import nl.jessedezwart.strongbuffs.panel.editor.ActionEditorRegistry;
import nl.jessedezwart.strongbuffs.panel.editor.ConditionEditorRegistry;
import nl.jessedezwart.strongbuffs.panel.state.RuleDraft;
import nl.jessedezwart.strongbuffs.panel.state.RulePanelController;
import nl.jessedezwart.strongbuffs.panel.state.RuleValidationResult;

public class RuleEditPanel extends JPanel
{
	private final RulePanelController controller;
	private final ConditionEditorRegistry conditionRegistry;
	private final ActionEditorRegistry actionRegistry;
	private final Runnable onLiveChange;
	private final Runnable onStructureChange;

	private RuleDraft draft;
	private RuleValidationResult validationResult = RuleValidationResult.valid();
	private JLabel nameErrorLabel;
	private JLabel conditionsErrorLabel;
	private JLabel actionErrorLabel;
	private JLabel emptyStateLabel;
	private JButton saveButton;
	private JButton cancelButton;
	private JTextField nameField;

	public RuleEditPanel(RulePanelController controller, ConditionEditorRegistry conditionRegistry,
			ActionEditorRegistry actionRegistry, Runnable onLiveChange, Runnable onStructureChange)
	{
		this.controller = controller;
		this.conditionRegistry = conditionRegistry;
		this.actionRegistry = actionRegistry;
		this.onLiveChange = onLiveChange;
		this.onStructureChange = onStructureChange;
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
	}

	public void refresh(RuleDraft draft, RuleValidationResult validationResult)
	{
		this.draft = draft;
		this.validationResult = validationResult == null ? RuleValidationResult.valid() : validationResult;
		removeAll();

		if (draft == null)
		{
			emptyStateLabel = new JLabel("Select a rule or create a new one.");
			emptyStateLabel.setFont(FontManager.getDefaultBoldFont());
			emptyStateLabel.setForeground(ColorScheme.TEXT_COLOR);
			add(emptyStateLabel, BorderLayout.NORTH);
			revalidate();
			repaint();
			return;
		}

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);
		content.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

		content.add(createBasicsSection());
		content.add(Box.createVerticalStrut(8));
		content.add(createConditionsSection());
		content.add(Box.createVerticalStrut(8));
		content.add(createActivationSection());
		content.add(Box.createVerticalStrut(8));
		content.add(createActionSection());
		content.add(Box.createVerticalStrut(8));
		content.add(createFooter());

		add(content, BorderLayout.NORTH);
		applyValidation(this.validationResult);
		revalidate();
		repaint();
	}

	public void applyValidation(RuleValidationResult validationResult)
	{
		this.validationResult = validationResult == null ? RuleValidationResult.valid() : validationResult;

		if (draft == null)
		{
			return;
		}

		if (nameErrorLabel != null)
		{
			nameErrorLabel.setText(valueOrBlank(this.validationResult.getFieldError(RulePanelController.FIELD_NAME)));
		}

		if (conditionsErrorLabel != null)
		{
			conditionsErrorLabel
					.setText(valueOrBlank(this.validationResult.getFieldError(RulePanelController.FIELD_CONDITIONS)));
		}

		if (actionErrorLabel != null)
		{
			String actionError = firstNonBlank(this.validationResult.getFieldError(RulePanelController.FIELD_ACTION),
					this.validationResult.getFieldError(RulePanelController.FIELD_ACTION_TEXT),
					this.validationResult.getFieldError(RulePanelController.FIELD_ACTION_COLOR),
					this.validationResult.getFieldError("action.durationTicks"),
					this.validationResult.getFieldError("action.soundKey"),
					this.validationResult.getFieldError("action.volumePercent"));
			actionErrorLabel.setText(valueOrBlank(actionError));
		}

		if (saveButton != null)
		{
			saveButton.setEnabled(this.validationResult.isValid());
		}
	}

	private JComponent createBasicsSection()
	{
		JPanel section = createSection("Basics");

		nameField = new JTextField(draft.getName() == null ? "" : draft.getName(), 16);
		nameField.getDocument().addDocumentListener(new SimpleDocumentListener(() ->
		{
			draft.setName(nameField.getText());
			onLiveChange.run();
		}));
		section.add(labeledRow("Name", nameField));

		nameErrorLabel = createErrorLabel();
		section.add(nameErrorLabel);

		javax.swing.JCheckBox enabledCheckBox = new javax.swing.JCheckBox("Enabled", draft.isEnabled());
		enabledCheckBox.setBackground(ColorScheme.DARK_GRAY_COLOR);
		enabledCheckBox.setForeground(ColorScheme.TEXT_COLOR);
		enabledCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		enabledCheckBox.addActionListener(event ->
		{
			draft.setEnabled(enabledCheckBox.isSelected());
			onLiveChange.run();
		});
		section.add(enabledCheckBox);
		return section;
	}

	private JComponent createConditionsSection()
	{
		JPanel section = createSection("Conditions");
		conditionsErrorLabel = createErrorLabel();
		section.add(conditionsErrorLabel);

		ConditionGroupPanel groupPanel = new ConditionGroupPanel(draft.getRootGroup(), true, conditionRegistry,
				onLiveChange, onStructureChange, null);
		groupPanel.setAlignmentX(LEFT_ALIGNMENT);
		groupPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		section.add(groupPanel);
		return section;
	}

	private JComponent createActivationSection()
	{
		JPanel section = createSection("Activation");

		JComboBox<ActivationMode> modeComboBox = new JComboBox<>(ActivationMode.values());
		modeComboBox.setSelectedItem(draft.getActivationMode());
		modeComboBox.addActionListener(event ->
		{
			draft.setActivationMode((ActivationMode) modeComboBox.getSelectedItem());
			onLiveChange.run();
		});
		section.add(labeledRow("Mode", modeComboBox));

		JSpinner cooldownSpinner = new JSpinner(new SpinnerNumberModel(draft.getCooldownTicks(), 0, 10000, 1));
		cooldownSpinner.addChangeListener(event ->
		{
			draft.setCooldownTicks((Integer) cooldownSpinner.getValue());
			onLiveChange.run();
		});
		section.add(labeledRow("Cooldown", cooldownSpinner));
		return section;
	}

	private JComponent createActionSection()
	{
		JPanel section = createSection("Action");

		JComboBox<Class<? extends ActionDefinition>> actionTypeComboBox = new JComboBox<>(
				actionRegistry.getActionClasses().toArray(new Class[0]));
		actionTypeComboBox.setRenderer((list, value, index, isSelected,
				cellHasFocus) -> new javax.swing.DefaultListCellRenderer().getListCellRendererComponent(list,
						value == null ? "" : actionRegistry.getByActionClass(value).getEditorLabel(), index, isSelected,
						cellHasFocus));
		actionTypeComboBox.setSelectedItem(draft.getAction().getClass());
		actionTypeComboBox.addActionListener(event ->
		{
			Class<? extends ActionDefinition> selectedClass = (Class<? extends ActionDefinition>) actionTypeComboBox
					.getSelectedItem();

			if (selectedClass == null || selectedClass == draft.getAction().getClass())
			{
				return;
			}

			draft.setAction(actionRegistry.createDefaultAction(selectedClass));
			onStructureChange.run();
		});
		section.add(labeledRow("Type", actionTypeComboBox));

		actionErrorLabel = createErrorLabel();
		section.add(actionErrorLabel);

		section.add(Box.createVerticalStrut(4));
		section.add(createActionEditor(draft.getAction()));
		return section;
	}

	private JComponent createFooter()
	{
		JPanel footer = new JPanel();
		footer.setLayout(new GridLayout(1, 2, 6, 0));
		footer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		footer.setAlignmentX(LEFT_ALIGNMENT);

		saveButton = new JButton("Save");
		saveButton.addActionListener(event ->
		{
			controller.saveDraft();
			onStructureChange.run();
		});
		footer.add(saveButton);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(event ->
		{
			controller.cancelDraft();
			onStructureChange.run();
		});
		footer.add(cancelButton);
		footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, footer.getPreferredSize().height));
		return footer;
	}

	private <T extends ActionDefinition> JComponent createActionEditor(ActionDefinition actionDefinition)
	{
		JComponent component = actionRegistry.createEditor(actionDefinition, onLiveChange);
		component.setAlignmentX(LEFT_ALIGNMENT);
		component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
		return component;
	}

	private static JPanel createSection(String title)
	{
		JPanel section = new JPanel();
		section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
		section.setBackground(ColorScheme.DARK_GRAY_COLOR);
		section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
				BorderFactory.createEmptyBorder(6, 6, 6, 6)));
		section.setAlignmentX(LEFT_ALIGNMENT);
		section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(FontManager.getDefaultBoldFont());
		titleLabel.setForeground(ColorScheme.TEXT_COLOR);
		titleLabel.setAlignmentX(LEFT_ALIGNMENT);
		section.add(titleLabel);
		section.add(Box.createVerticalStrut(6));
		return section;
	}

	private static JPanel labeledRow(String label, JComponent component)
	{
		JPanel row = new JPanel(new BorderLayout(8, 0));
		row.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row.setAlignmentX(LEFT_ALIGNMENT);

		JLabel title = new JLabel(label);
		title.setForeground(ColorScheme.TEXT_COLOR);
		title.setPreferredSize(new Dimension(70, title.getPreferredSize().height));
		row.add(title, BorderLayout.WEST);
		component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
		row.add(component, BorderLayout.CENTER);
		return row;
	}

	private static JLabel createErrorLabel()
	{
		JLabel label = new JLabel(" ");
		label.setForeground(new Color(255, 120, 120));
		label.setAlignmentX(LEFT_ALIGNMENT);
		return label;
	}

	private static String firstNonBlank(String... values)
	{
		for (String value : values)
		{
			if (value != null && !value.trim().isEmpty())
			{
				return value;
			}
		}

		return null;
	}

	private static String valueOrBlank(String value)
	{
		return value == null || value.trim().isEmpty() ? " " : value;
	}

	private static final class SimpleDocumentListener implements DocumentListener
	{
		private final Runnable callback;

		private SimpleDocumentListener(Runnable callback)
		{
			this.callback = callback;
		}

		@Override
		public void insertUpdate(DocumentEvent event)
		{
			callback.run();
		}

		@Override
		public void removeUpdate(DocumentEvent event)
		{
			callback.run();
		}

		@Override
		public void changedUpdate(DocumentEvent event)
		{
			callback.run();
		}
	}
}
