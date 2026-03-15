package nl.jessedezwart.strongbuffs.panel.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;
import nl.jessedezwart.strongbuffs.panel.editor.ConditionEditorRegistry;
import net.runelite.client.ui.ColorScheme;

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
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		setAlignmentX(LEFT_ALIGNMENT);

		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		left.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JComboBox<Class<? extends NumericConditionDefinition>> typeComboBox = new JComboBox<>(
				conditionRegistry.getConditionClasses().toArray(new Class[0]));
		typeComboBox.setRenderer((list, value, index, isSelected,
				cellHasFocus) -> new javax.swing.DefaultListCellRenderer().getListCellRendererComponent(list,
						value == null ? "" : conditionRegistry.getByConditionClass(value).getEditorLabel(), index,
						isSelected, cellHasFocus));
		typeComboBox.setSelectedItem(condition.getClass());
		typeComboBox.setPreferredSize(new Dimension(120, typeComboBox.getPreferredSize().height));
		typeComboBox.setMaximumSize(typeComboBox.getPreferredSize());
		typeComboBox.addActionListener(event ->
		{
			Class<? extends NumericConditionDefinition> selectedClass = (Class<? extends NumericConditionDefinition>) typeComboBox
					.getSelectedItem();
			Class<? extends NumericConditionDefinition> currentClass = condition.getClass()
					.asSubclass(NumericConditionDefinition.class);

			if (Objects.equals(selectedClass, currentClass))
			{
				return;
			}

			onReplace.accept(conditionRegistry.createDefaultCondition(selectedClass));
			onStructureChange.run();
		});
		left.add(typeComboBox);
		add(left);
		add(Box.createHorizontalStrut(6));

		JPanel editorPanel = createEditorPanel();
		editorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, editorPanel.getPreferredSize().height));
		add(editorPanel);
		add(Box.createHorizontalStrut(6));

		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(event -> onRemove.run());
		removeButton.setMaximumSize(removeButton.getPreferredSize());
		add(removeButton);
	}

	private JPanel createEditorPanel()
	{
		JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		center.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		center.add(conditionRegistry.createEditor(condition, onLiveChange));
		return center;
	}
}
