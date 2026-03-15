package nl.jessedezwart.strongbuffs.panel.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.panel.editor.ActionEditorRegistry;
import nl.jessedezwart.strongbuffs.panel.state.RuleDescriptions;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class RuleListPanel extends JPanel
{
	private final DefaultListModel<RuleDefinition> listModel = new DefaultListModel<>();
	private final JList<RuleDefinition> ruleList = new JList<>(listModel);
	private final JButton duplicateButton = new JButton("Duplicate");
	private final JButton deleteButton = new JButton("Delete");
	private boolean suppressSelectionEvents;

	public RuleListPanel(Consumer<String> onSelectRule, Runnable onCreateRule, Runnable onDuplicateRule,
			Runnable onDeleteRule, ActionEditorRegistry actionRegistry)
	{
		setLayout(new BorderLayout(0, 6));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(BorderFactory.createTitledBorder("Rules"));

		JPanel actions = new JPanel(new GridLayout(1, 3, 6, 0));
		actions.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton newButton = new JButton("New Rule");
		newButton.addActionListener(event -> onCreateRule.run());
		actions.add(newButton);

		duplicateButton.addActionListener(event -> onDuplicateRule.run());
		actions.add(duplicateButton);

		deleteButton.addActionListener(event -> onDeleteRule.run());
		actions.add(deleteButton);

		add(actions, BorderLayout.NORTH);

		ruleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ruleList.setCellRenderer(new RuleCellRenderer(actionRegistry));
		ruleList.addListSelectionListener(event ->
		{
			if (event.getValueIsAdjusting() || suppressSelectionEvents)
			{
				return;
			}

			RuleDefinition selected = ruleList.getSelectedValue();
			onSelectRule.accept(selected == null ? null : selected.getId());
		});

		JScrollPane scrollPane = new JScrollPane(ruleList);
		scrollPane.setPreferredSize(new Dimension(0, 160));
		scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
		add(scrollPane, BorderLayout.CENTER);
	}

	public void refresh(List<RuleDefinition> rules, String selectedRuleId)
	{
		suppressSelectionEvents = true;
		listModel.clear();

		for (RuleDefinition ruleDefinition : rules)
		{
			listModel.addElement(ruleDefinition);
		}

		if (selectedRuleId == null)
		{
			ruleList.clearSelection();
		}
		else
		{
			for (int i = 0; i < listModel.size(); i++)
			{
				if (selectedRuleId.equals(listModel.get(i).getId()))
				{
					ruleList.setSelectedIndex(i);
					break;
				}
			}
		}

		boolean hasSelection = selectedRuleId != null;
		duplicateButton.setEnabled(hasSelection);
		deleteButton.setEnabled(hasSelection);
		suppressSelectionEvents = false;
	}

	private static final class RuleCellRenderer implements ListCellRenderer<RuleDefinition>
	{
		private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		private final ActionEditorRegistry actionRegistry;

		private RuleCellRenderer(ActionEditorRegistry actionRegistry)
		{
			this.actionRegistry = actionRegistry;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends RuleDefinition> list, RuleDefinition value,
				int index, boolean isSelected, boolean cellHasFocus)
		{
			JPanel panel = new JPanel(new BorderLayout(0, 2));
			panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
			panel.setBackground(isSelected ? ColorScheme.DARK_GRAY_HOVER_COLOR : ColorScheme.DARKER_GRAY_COLOR);

			javax.swing.JLabel title = (javax.swing.JLabel) defaultRenderer.getListCellRendererComponent(list,
					value == null || value.getName() == null || value.getName().trim().isEmpty() ? "Unnamed rule"
							: value.getName(),
					index, isSelected, cellHasFocus);
			title.setFont(FontManager.getDefaultBoldFont());
			title.setForeground(ColorScheme.TEXT_COLOR);
			title.setOpaque(false);
			panel.add(title, BorderLayout.NORTH);

			String enabledSummary = value != null && value.isEnabled() ? "Enabled" : "Disabled";
			String metaText = enabledSummary + " | " + RuleDescriptions.describeRule(value, actionRegistry);
			javax.swing.JLabel meta = new javax.swing.JLabel(metaText);
			meta.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			panel.add(meta, BorderLayout.CENTER);
			return panel;
		}
	}
}
