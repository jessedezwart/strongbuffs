package nl.jessedezwart.strongbuffs.panel.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionLogic;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.NumericConditionDefinition;
import nl.jessedezwart.strongbuffs.panel.editor.ConditionEditorRegistry;
import net.runelite.client.ui.ColorScheme;

public class ConditionGroupPanel extends JPanel
{
	private final ConditionGroup group;
	private final boolean root;
	private final ConditionEditorRegistry conditionRegistry;
	private final Runnable onLiveChange;
	private final Runnable onStructureChange;
	private final Runnable onRemove;

	public ConditionGroupPanel(ConditionGroup group, boolean root, ConditionEditorRegistry conditionRegistry,
		Runnable onLiveChange, Runnable onStructureChange, Runnable onRemove)
	{
		this.group = group;
		this.root = root;
		this.conditionRegistry = conditionRegistry;
		this.onLiveChange = onLiveChange;
		this.onStructureChange = onStructureChange;
		this.onRemove = onRemove;
		build();
	}

	void addCondition()
	{
		Class<? extends NumericConditionDefinition> conditionClass = conditionRegistry.getConditionClasses().get(0);
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
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
			BorderFactory.createEmptyBorder(6, 6, 6, 6)));

		add(createHeader());
		add(Box.createVerticalStrut(6));

		for (ConditionNode child : group.getChildren())
		{
			if (child instanceof ConditionGroup)
			{
				ConditionGroup childGroup = (ConditionGroup) child;
				ConditionGroupPanel childPanel = new ConditionGroupPanel(childGroup, false, conditionRegistry,
					onLiveChange, onStructureChange, () -> removeChild(childGroup));
				childPanel.setAlignmentX(LEFT_ALIGNMENT);
				add(childPanel);
			}
			else
			{
				ConditionDefinition condition = (ConditionDefinition) child;
				ConditionRowPanel rowPanel = new ConditionRowPanel(condition, conditionRegistry, onLiveChange,
					onStructureChange, () -> removeChild(condition), replacement -> replaceChild(condition, replacement));
				rowPanel.setAlignmentX(LEFT_ALIGNMENT);
				add(rowPanel);
			}

			add(Box.createVerticalStrut(6));
		}

		add(createFooter());
	}

	private JPanel createHeader()
	{
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		left.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JComboBox<ConditionLogic> logicComboBox = new JComboBox<>(ConditionLogic.values());
		logicComboBox.setSelectedItem(group.getLogic());
		logicComboBox.addActionListener(event ->
		{
			group.setLogic((ConditionLogic) logicComboBox.getSelectedItem());
			onLiveChange.run();
		});
		left.add(logicComboBox);
		header.add(left, BorderLayout.WEST);

		if (!root && onRemove != null)
		{
			JButton removeGroupButton = new JButton("Remove group");
			removeGroupButton.addActionListener(event -> onRemove.run());
			header.add(removeGroupButton, BorderLayout.EAST);
		}

		return header;
	}

	private JPanel createFooter()
	{
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		footer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JButton addConditionButton = new JButton("Add condition");
		addConditionButton.addActionListener(event -> addCondition());
		footer.add(addConditionButton);

		footer.add(Box.createHorizontalStrut(6));

		JButton addGroupButton = new JButton("Add group");
		addGroupButton.addActionListener(event -> addGroup());
		footer.add(addGroupButton);
		return footer;
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
