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
import javax.swing.JPanel;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionLogic;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;
import nl.jessedezwart.strongbuffs.panel.editor.ConditionEditorRegistry;
import net.runelite.client.ui.ColorScheme;

/**
 * Recursive Swing view for one condition-group branch and its children.
 */
public class ConditionGroupPanel extends JPanel
{
	private final ConditionGroup group;
	private final boolean root;
	private final ConditionEditorRegistry conditionRegistry;
	private final Runnable onLiveChange;
	private final Runnable onStructureChange;
	private final Runnable onRemove;
	private final int depth;

	public ConditionGroupPanel(ConditionGroup group, boolean root, ConditionEditorRegistry conditionRegistry,
			Runnable onLiveChange, Runnable onStructureChange, Runnable onRemove)
	{
		this(group, root, root ? 0 : 1, conditionRegistry, onLiveChange, onStructureChange, onRemove);
	}

	private ConditionGroupPanel(ConditionGroup group, boolean root, int depth, ConditionEditorRegistry conditionRegistry,
			Runnable onLiveChange, Runnable onStructureChange, Runnable onRemove)
	{
		this.group = group;
		this.root = root;
		this.depth = depth;
		this.conditionRegistry = conditionRegistry;
		this.onLiveChange = onLiveChange;
		this.onStructureChange = onStructureChange;
		this.onRemove = onRemove;
		build();
	}

	void addCondition()
	{
		Class<? extends ConditionDefinition> conditionClass = conditionRegistry.getConditionClasses().get(0);
		group.getChildren().add(conditionRegistry.createDefaultCondition(conditionClass));
		onStructureChange.run();
	}

	void addGroup()
	{
		group.getChildren().add(new ConditionGroup());
		onStructureChange.run();
	}

	void removeChild(ConditionNode child)
	{
		group.getChildren().remove(child);
		onStructureChange.run();
	}

	private void build()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setAlignmentX(LEFT_ALIGNMENT);
		setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		setBorder(createGroupBorder());

		add(createHeader());

		for (int i = 0; i < group.getChildren().size(); i++)
		{
			ConditionNode child = group.getChildren().get(i);

			if (child instanceof ConditionGroup)
			{
				ConditionGroup childGroup = (ConditionGroup) child;
				ConditionGroupPanel childPanel = new ConditionGroupPanel(childGroup, false, depth + 1, conditionRegistry,
						onLiveChange, onStructureChange, () -> removeChild(childGroup));
				childPanel.setAlignmentX(LEFT_ALIGNMENT);
				add(childPanel);
			}
			else
			{
				ConditionDefinition condition = (ConditionDefinition) child;
				ConditionRowPanel rowPanel = new ConditionRowPanel(condition, conditionRegistry, onLiveChange,
						onStructureChange, () -> removeChild(condition),
						replacement -> replaceChild(condition, replacement));
				rowPanel.setAlignmentX(LEFT_ALIGNMENT);
				add(rowPanel);
			}

			if (i < group.getChildren().size() - 1)
			{
				add(Box.createVerticalStrut(4));
			}
		}

		add(Box.createVerticalStrut(4));
		add(createFooter());
	}

	private JPanel createHeader()
	{
		JPanel header = new JPanel(new BorderLayout(6, 0));
		header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		header.setAlignmentX(LEFT_ALIGNMENT);
		header.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		JComboBox<ConditionLogic> logicComboBox = new JComboBox<>(ConditionLogic.values());
		logicComboBox.setSelectedItem(group.getLogic());
		logicComboBox.setPreferredSize(new Dimension(72, logicComboBox.getPreferredSize().height));
		logicComboBox.setMaximumSize(logicComboBox.getPreferredSize());
		logicComboBox.addActionListener(event ->
		{
			int selectedIndex = logicComboBox.getSelectedIndex();

			if (selectedIndex < 0)
			{
				return;
			}

			group.setLogic(logicComboBox.getItemAt(selectedIndex));
			onLiveChange.run();
		});
		header.add(logicComboBox, BorderLayout.WEST);

		if (!root && onRemove != null)
		{
			JButton removeGroupButton = new JButton("X");
			removeGroupButton.setToolTipText("Remove group");
			removeGroupButton.setFocusable(false);
			removeGroupButton.addActionListener(event -> onRemove.run());
			configureCompactButton(removeGroupButton);
			header.add(removeGroupButton, BorderLayout.EAST);
		}

		return header;
	}

	private JPanel createFooter()
	{
		JPanel footer = new JPanel(new GridLayout(1, 2, 6, 0));
		footer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		footer.setAlignmentX(LEFT_ALIGNMENT);

		JButton addConditionButton = new JButton(root ? "+ Condition" : "+ Cond");
		addConditionButton.addActionListener(event -> addCondition());
		footer.add(addConditionButton);

		JButton addGroupButton = new JButton(root ? "+ Group" : "+ Grp");
		addGroupButton.addActionListener(event -> addGroup());
		footer.add(addGroupButton);
		footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, footer.getPreferredSize().height));
		return footer;
	}

	private javax.swing.border.Border createGroupBorder()
	{
		if (root)
		{
			return BorderFactory.createEmptyBorder();
		}

		Color borderColor = depth == 1 ? ColorScheme.MEDIUM_GRAY_COLOR : ColorScheme.DARKER_GRAY_HOVER_COLOR;
		return BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 1, 0, 0, borderColor),
				BorderFactory.createEmptyBorder(0, 6, 0, 0));
	}

	private static void configureCompactButton(JButton button)
	{
		button.setMargin(new java.awt.Insets(2, 4, 2, 4));
		button.setPreferredSize(new Dimension(28, button.getPreferredSize().height));
		button.setMaximumSize(button.getPreferredSize());
	}

	private void replaceChild(ConditionDefinition existingCondition, ConditionDefinition replacement)
	{
		int index = group.getChildren().indexOf(existingCondition);

		if (index < 0)
		{
			return;
		}

		group.getChildren().set(index, replacement);
	}
}
