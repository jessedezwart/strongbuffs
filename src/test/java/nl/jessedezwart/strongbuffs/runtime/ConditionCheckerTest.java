package nl.jessedezwart.strongbuffs.runtime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import nl.jessedezwart.strongbuffs.model.condition.ComparisonOperator;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.ConditionLogic;
import nl.jessedezwart.strongbuffs.model.condition.impl.BankValueCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.GroundItemCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.HpCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.InventoryValueCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.ItemCountCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.ItemEquippedCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.ItemInInventoryCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.ItemPriceCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PlayerInInstanceCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PoisonCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerActiveCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.PrayerPointsCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.RunEnergyCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SkillLevelCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SlayerTaskCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.SpecialAttackCondition;
import nl.jessedezwart.strongbuffs.model.condition.impl.XpGainCondition;
import nl.jessedezwart.strongbuffs.runtime.condition.ConditionTreeEvaluator;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.impl.VarRuntimeState;
import org.junit.Before;
import org.junit.Test;

public class ConditionCheckerTest
{
	private final ConditionTreeEvaluator checker = new ConditionTreeEvaluator();
	private RuntimeState state;

	@Before
	public void setUp()
	{
		state = new RuntimeState();
		state.getSkills().setHitpoints(27);
		state.getSkills().setPrayerPoints(9);
		state.getSkills().setCurrentTick(1);
		state.getVars().setSpecialAttackPercent(100);
		state.getLocation().setRunEnergyPercent(35);
		state.getVars().setPoisonState(VarRuntimeState.PoisonState.VENOM);
		state.getVars().setSlayerTaskActive(true);
		state.getVars().setSlayerTaskRemaining(12);
		state.getSkills().setRealSkillLevel(Skill.ATTACK, 70);
		state.getSkills().markXpGain(Skill.ATTACK);
		state.getInventory().setInventoryItemCount("Shark", 3);
		state.getInventory().setEquippedItems(Arrays.asList("Dragon scimitar", "Fire cape"));
		state.getGroundItems().incrementNearbyGroundItem("Coins");
		state.getGroundItems().incrementNearbyGroundItem("Rune arrow");
		state.getVars().setActivePrayers(Arrays.asList(Prayer.THICK_SKIN, Prayer.BURST_OF_STRENGTH));
		state.getLocation().setPlayerLocation(new WorldPoint(3200, 3205, 0));
		state.getLocation().setInInstance(true);
		state.getInventory().setItemPrice("abyssal whip", 2500000L);
		state.getInventory().setInventoryTotalValue(750000L);
		state.getInventory().setBankTotalValue(150000000L);
	}

	@Test
	public void evaluatesHitpointsCondition()
	{
		HpCondition condition = new HpCondition();
		condition.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
		condition.setThreshold(30);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesPrayerPointsCondition()
	{
		PrayerPointsCondition condition = new PrayerPointsCondition();
		condition.setOperator(ComparisonOperator.LESS_THAN);
		condition.setThreshold(10);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesPrayerActiveCondition()
	{
		PrayerActiveCondition condition = new PrayerActiveCondition();
		condition.setPrayer(Prayer.THICK_SKIN);
		condition.setActive(true);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesSpecialAttackCondition()
	{
		SpecialAttackCondition condition = new SpecialAttackCondition();
		condition.setOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		condition.setThreshold(100);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesRunEnergyCondition()
	{
		RunEnergyCondition condition = new RunEnergyCondition();
		condition.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
		condition.setThreshold(35);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesPoisonCondition()
	{
		PoisonCondition condition = new PoisonCondition();
		condition.setPoisonType(PoisonCondition.PoisonType.VENOM);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesSlayerTaskActiveCondition()
	{
		SlayerTaskCondition condition = new SlayerTaskCondition();
		condition.setCheck(SlayerTaskCondition.SlayerTaskCheck.TASK_ACTIVE);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesSlayerTaskRemainingCondition()
	{
		SlayerTaskCondition condition = new SlayerTaskCondition();
		condition.setCheck(SlayerTaskCondition.SlayerTaskCheck.KILLS_REMAINING);
		condition.setOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		condition.setThreshold(10);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesSkillLevelCondition()
	{
		SkillLevelCondition condition = new SkillLevelCondition();
		condition.setSkill(Skill.ATTACK);
		condition.setOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		condition.setThreshold(70);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesXpGainCondition()
	{
		XpGainCondition condition = new XpGainCondition();
		condition.setSkill(Skill.ATTACK);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesInventoryItemCondition()
	{
		ItemInInventoryCondition condition = new ItemInInventoryCondition();
		condition.setItemName(" shark ");

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesItemCountCondition()
	{
		ItemCountCondition condition = new ItemCountCondition();
		condition.setItemName("SHARK");
		condition.setOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		condition.setThreshold(3);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesItemEquippedCondition()
	{
		ItemEquippedCondition condition = new ItemEquippedCondition();
		condition.setItemName("fire cape");

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesGroundItemCondition()
	{
		GroundItemCondition condition = new GroundItemCondition();
		condition.setItemName("rune arrow");

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesItemPriceCondition()
	{
		ItemPriceCondition condition = new ItemPriceCondition();
		condition.setItemName("Abyssal whip");
		condition.setOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		condition.setThreshold(2000000);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesInventoryValueCondition()
	{
		InventoryValueCondition condition = new InventoryValueCondition();
		condition.setOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		condition.setThreshold(500000);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesBankValueCondition()
	{
		BankValueCondition condition = new BankValueCondition();
		condition.setOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL);
		condition.setThreshold(100000000);

		assertTrue(checker.evaluate(condition, state));
	}

	@Test
	public void evaluatesPlayerInInstanceCondition()
	{
		assertTrue(checker.evaluate(new PlayerInInstanceCondition(), state));
	}

	@Test
	public void evaluatesNestedConditionGroups()
	{
		HpCondition hpCondition = new HpCondition();
		hpCondition.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
		hpCondition.setThreshold(30);

		ItemInInventoryCondition inventoryCondition = new ItemInInventoryCondition();
		inventoryCondition.setItemName("Shark");

		ItemEquippedCondition equippedCondition = new ItemEquippedCondition();
		equippedCondition.setItemName("Infernal cape");

		ConditionGroup nestedGroup = new ConditionGroup();
		nestedGroup.setLogic(ConditionLogic.OR);
		nestedGroup.getChildren().add(inventoryCondition);
		nestedGroup.getChildren().add(equippedCondition);

		ConditionGroup rootGroup = new ConditionGroup();
		rootGroup.setLogic(ConditionLogic.AND);
		rootGroup.getChildren().add(hpCondition);
		rootGroup.getChildren().add(nestedGroup);

		assertTrue(checker.evaluate(rootGroup, state));
	}

	@Test
	public void returnsFalseForNonMatchingCondition()
	{
		RunEnergyCondition condition = new RunEnergyCondition();
		condition.setOperator(ComparisonOperator.GREATER_THAN);
		condition.setThreshold(90);

		assertFalse(checker.evaluate(condition, state));
	}
}
