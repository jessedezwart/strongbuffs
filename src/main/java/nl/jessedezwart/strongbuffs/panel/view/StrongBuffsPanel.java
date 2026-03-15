package nl.jessedezwart.strongbuffs.panel.view;

import java.awt.BorderLayout;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import nl.jessedezwart.strongbuffs.panel.editor.ActionEditorRegistry;
import nl.jessedezwart.strongbuffs.panel.editor.ConditionEditorRegistry;
import nl.jessedezwart.strongbuffs.panel.state.RuleControllerActionResult;
import nl.jessedezwart.strongbuffs.panel.state.RulePanelController;
import nl.jessedezwart.strongbuffs.panel.state.UnsavedResolution;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * Root RuneLite sidebar panel for managing rule definitions and editing the selected draft.
 */
@Singleton
public class StrongBuffsPanel extends PluginPanel
{
	private final RulePanelController controller;
	private final RuleListPanel ruleListPanel;
	private final RuleEditPanel ruleEditPanel;

	@Inject
	public StrongBuffsPanel(RulePanelController controller, ConditionEditorRegistry conditionRegistry,
		ActionEditorRegistry actionRegistry)
	{
		super(true);
		this.controller = controller;

		JPanel wrappedPanel = getWrappedPanel();
		wrappedPanel.setLayout(new BorderLayout());
		wrappedPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrappedPanel.add(content, BorderLayout.NORTH);

		ruleListPanel = new RuleListPanel(this::selectRule, this::createRule, this::duplicateRule, this::deleteRule,
			actionRegistry);
		content.add(ruleListPanel);
		content.add(Box.createVerticalStrut(8));

		ruleEditPanel = new RuleEditPanel(controller, conditionRegistry, actionRegistry,
			this::handleLiveDraftChange, this::refreshFromController);
		content.add(ruleEditPanel);

		refreshFromController();
	}

	public void reload()
	{
		controller.reload();
		refreshFromController();
	}

	private void selectRule(String ruleId)
	{
		handleControllerAction(controller.requestSelectRule(ruleId));
	}

	private void createRule()
	{
		handleControllerAction(controller.requestCreateRule());
	}

	private void duplicateRule()
	{
		handleControllerAction(controller.requestDuplicateSelectedRule());
	}

	private void deleteRule()
	{
		if (controller.getSelectedRuleId() == null)
		{
			return;
		}

		int choice = JOptionPane.showConfirmDialog(this, "Delete the selected rule?", "Delete Rule",
			JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (choice != JOptionPane.YES_OPTION)
		{
			return;
		}

		handleControllerAction(controller.requestDeleteSelectedRule());
	}

	private void handleControllerAction(RuleControllerActionResult result)
	{
		if (result.requiresUnsavedConfirmation())
		{
			int choice = JOptionPane.showOptionDialog(this, "You have unsaved changes.", "Unsaved Changes",
				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
				new Object[] {"Save", "Discard", "Cancel"}, "Save");

			UnsavedResolution resolution;

			if (choice == 0)
			{
				resolution = UnsavedResolution.SAVE;
			}
			else if (choice == 1)
			{
				resolution = UnsavedResolution.DISCARD;
			}
			else
			{
				resolution = UnsavedResolution.CANCEL;
			}

			result = controller.resolvePendingAction(resolution);
		}

		if (!result.getValidationResult().isValid())
		{
			controller.revalidateDraft();
		}

		refreshFromController();
	}

	private void handleLiveDraftChange()
	{
		controller.revalidateDraft();
		ruleListPanel.refresh(controller.getVisibleRules(), controller.getSelectedRuleId());
		ruleEditPanel.applyValidation(controller.getValidationResult());
	}

	private void refreshFromController()
	{
		controller.revalidateDraft();
		ruleListPanel.refresh(controller.getVisibleRules(), controller.getSelectedRuleId());
		ruleEditPanel.refresh(controller.getDraft(), controller.getValidationResult());

		SwingUtilities.invokeLater(() ->
		{
			ruleEditPanel.scrollRectToVisible(ruleEditPanel.getBounds());
			revalidate();
			repaint();
		});
	}
}
