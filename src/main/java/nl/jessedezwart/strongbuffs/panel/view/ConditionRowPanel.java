package nl.jessedezwart.strongbuffs.panel.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.panel.editor.ConditionEditorRegistry;
import net.runelite.client.ui.ColorScheme;

/**
 * View component for one leaf condition row inside a condition group.
 */
public class ConditionRowPanel extends JPanel
{
	private final ConditionDefinition condition;
	private final ConditionEditorRegistry conditionRegistry;
	private final Runnable onLiveChange;
	private final Runnable onStructureChange;
	private final Runnable onRemove;
	private final Consumer<ConditionDefinition> onReplace;

	public ConditionRowPanel(ConditionDefinition condition, ConditionEditorRegistry conditionRegistry,
			Runnable onLiveChange, Runnable onStructureChange, Runnable onRemove,
			Consumer<ConditionDefinition> onReplace)
	{
		this.condition = condition;
		this.conditionRegistry = conditionRegistry;
		this.onLiveChange = onLiveChange;
		this.onStructureChange = onStructureChange;
		this.onRemove = onRemove;
		this.onReplace = onReplace;
		build();
	}

	private void build()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		setAlignmentX(LEFT_ALIGNMENT);

		JPanel header = new JPanel(new BorderLayout(6, 0));
		header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		header.setAlignmentX(LEFT_ALIGNMENT);

		DefaultComboBoxModel<Class<? extends ConditionDefinition>> typeModel = new DefaultComboBoxModel<>();

		for (Class<? extends ConditionDefinition> conditionClass : conditionRegistry.getConditionClasses())
		{
			typeModel.addElement(conditionClass);
		}

		JComboBox<Class<? extends ConditionDefinition>> typeComboBox = new JComboBox<>(typeModel);
		typeComboBox.setRenderer((list, value, index, isSelected,
				cellHasFocus) -> new javax.swing.DefaultListCellRenderer().getListCellRendererComponent(list,
						value == null ? "" : conditionRegistry.getByConditionClass(value).getEditorLabel(), index,
						isSelected, cellHasFocus));
		typeComboBox.setSelectedItem(condition.getClass());
		typeComboBox.setPreferredSize(new Dimension(112, typeComboBox.getPreferredSize().height));
		typeComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, typeComboBox.getPreferredSize().height));
		typeComboBox.addActionListener(event ->
		{
			int selectedIndex = typeComboBox.getSelectedIndex();
			Class<? extends ConditionDefinition> selectedClass = selectedIndex < 0 ? null
					: typeModel.getElementAt(selectedIndex);
			Class<? extends ConditionDefinition> currentClass = condition.getClass()
					.asSubclass(ConditionDefinition.class);

			if (Objects.equals(selectedClass, currentClass))
			{
				return;
			}

			onReplace.accept(conditionRegistry.createDefaultCondition(selectedClass));
			onStructureChange.run();
		});
		header.add(typeComboBox, BorderLayout.CENTER);

		JButton removeButton = new JButton("X");
		removeButton.setToolTipText("Remove condition");
		removeButton.setFocusable(false);
		removeButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
		removeButton.addActionListener(event -> onRemove.run());
		removeButton.setPreferredSize(new Dimension(28, removeButton.getPreferredSize().height));
		removeButton.setMaximumSize(removeButton.getPreferredSize());
		header.add(removeButton, BorderLayout.EAST);
		header.setMaximumSize(new Dimension(Integer.MAX_VALUE, header.getPreferredSize().height));
		add(header);

		add(Box.createVerticalStrut(4));

		JComponent editorPanel = createEditorPanel();
		editorPanel.setAlignmentX(LEFT_ALIGNMENT);
		add(editorPanel);
		setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
	}

	private JComponent createEditorPanel()
	{
		return conditionRegistry.createEditor(condition, onLiveChange);
	}
}
