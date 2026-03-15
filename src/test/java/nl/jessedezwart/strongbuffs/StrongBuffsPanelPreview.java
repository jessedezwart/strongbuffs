package nl.jessedezwart.strongbuffs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import nl.jessedezwart.strongbuffs.model.action.impl.OverlayTextAction;
import nl.jessedezwart.strongbuffs.model.action.impl.ScreenFlashAction;
import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerPointsCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SpecialAttackCondition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionLogic;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.panel.editor.ActionEditorRegistry;
import nl.jessedezwart.strongbuffs.panel.editor.ConditionEditorRegistry;
import nl.jessedezwart.strongbuffs.panel.state.RulePanelController;
import nl.jessedezwart.strongbuffs.panel.view.StrongBuffsPanel;
import net.runelite.client.ui.laf.RuneLiteLAF;

public class StrongBuffsPanelPreview
{
	public static void main(String[] args)
	{
		RuneLiteLAF.setup();
		SwingUtilities.invokeLater(StrongBuffsPanelPreview::showPreview);
	}

	private static void showPreview()
	{
		ConditionEditorRegistry conditionRegistry = new ConditionEditorRegistry();
		ActionEditorRegistry actionRegistry = new ActionEditorRegistry();
		InMemoryRuleDefinitionStore store = new InMemoryRuleDefinitionStore(createSampleRules());
		RulePanelController controller = new RulePanelController(store, conditionRegistry, actionRegistry);

		List<RuleDefinition> rules = controller.getVisibleRules();

		if (!rules.isEmpty())
		{
			controller.requestSelectRule(rules.get(0).getId());
		}

		StrongBuffsPanel panel = new StrongBuffsPanel(controller, conditionRegistry, actionRegistry);

		JFrame frame = new JFrame("Strong Buffs UI Preview");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setContentPane(panel.getWrappedPanel());
		frame.setSize(242, 760);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}

	private static List<RuleDefinition> createSampleRules()
	{
		List<RuleDefinition> rules = new ArrayList<>();
		rules.add(createLowHpRule());
		rules.add(createPrayerRule());
		return rules;
	}

	private static RuleDefinition createLowHpRule()
	{
		HpCondition hpCondition = new HpCondition();
		hpCondition.setThreshold(30);

		SpecialAttackCondition specCondition = new SpecialAttackCondition();
		specCondition.setThreshold(100);

		ConditionGroup nestedGroup = new ConditionGroup();
		nestedGroup.setLogic(ConditionLogic.OR);
		nestedGroup.getChildren().add(specCondition);

		ConditionGroup rootGroup = new ConditionGroup();
		rootGroup.setLogic(ConditionLogic.AND);
		rootGroup.getChildren().add(hpCondition);
		rootGroup.getChildren().add(nestedGroup);

		ScreenFlashAction action = new ScreenFlashAction();
		action.setColorHex("#FF3300");
		action.setDurationTicks(3);

		RuleDefinition rule = new RuleDefinition();
		rule.setId(UUID.randomUUID().toString());
		rule.setName("Low HP Warning");
		rule.setEnabled(true);
		rule.setRootGroup(rootGroup);
		rule.setAction(action);
		return rule;
	}

	private static RuleDefinition createPrayerRule()
	{
		PrayerPointsCondition prayerCondition = new PrayerPointsCondition();
		prayerCondition.setThreshold(10);

		ConditionGroup rootGroup = new ConditionGroup();
		rootGroup.setLogic(ConditionLogic.AND);
		rootGroup.getChildren().add(prayerCondition);

		OverlayTextAction action = new OverlayTextAction();
		action.setText("Restore prayer");
		action.setColorHex("#00FFCC");
		action.setShowValue(true);

		RuleDefinition rule = new RuleDefinition();
		rule.setId(UUID.randomUUID().toString());
		rule.setName("Low Prayer");
		rule.setEnabled(true);
		rule.setRootGroup(rootGroup);
		rule.setAction(action);
		return rule;
	}

	private static final class InMemoryRuleDefinitionStore extends RuleDefinitionStore
	{
		private final List<RuleDefinition> rules = new ArrayList<>();

		private InMemoryRuleDefinitionStore(List<RuleDefinition> initialRules)
		{
			super(null, null);
			rules.addAll(initialRules);
		}

		@Override
		public List<RuleDefinition> load()
		{
			return copyRules(rules);
		}

		@Override
		public void save(List<RuleDefinition> updatedRules)
		{
			rules.clear();
			rules.addAll(copyRules(updatedRules));
		}

		private static List<RuleDefinition> copyRules(List<RuleDefinition> sourceRules)
		{
			List<RuleDefinition> copies = new ArrayList<>();

			for (RuleDefinition sourceRule : sourceRules)
			{
				RuleDefinition copy = new RuleDefinition();
				copy.setSchemaVersion(sourceRule.getSchemaVersion());
				copy.setId(sourceRule.getId());
				copy.setName(sourceRule.getName());
				copy.setEnabled(sourceRule.isEnabled());
				copy.setRootGroup(
						nl.jessedezwart.strongbuffs.panel.state.RuleDraft.copyGroup(sourceRule.getRootGroup()));
				copy.setActivationMode(sourceRule.getActivationMode());
				copy.setCooldownTicks(sourceRule.getCooldownTicks());
				copy.setAction(nl.jessedezwart.strongbuffs.panel.state.RuleDraft.copyAction(sourceRule.getAction()));
				copies.add(copy);
			}

			return copies;
		}
	}
}
