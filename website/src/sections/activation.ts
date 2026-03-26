import { replaceChildren, createFieldGrid } from "../dom";
import { createSelectField, createNumberField } from "../fields";
import type { DomRefs, Manifest, RuleState } from "../types";

interface ActivationActions
{
	updateRuleField: (key: keyof RuleState, value: unknown) => void;
}

export function renderActivationSection(
	domRefs: DomRefs,
	manifest: Manifest,
	rule: RuleState,
	actions: ActivationActions
): void
{
	const grid = createFieldGrid();

	grid.appendChild(
		createSelectField(
			"Mode",
			manifest.activationModes,
			rule.activationMode,
			function (value)
			{
				actions.updateRuleField("activationMode", value);
			}
		)
	);

	grid.appendChild(
		createNumberField(
			"Cooldown ticks",
			rule.cooldownTicks,
			function (value)
			{
				actions.updateRuleField("cooldownTicks", value);
			},
			{ min: 0, max: 10000, step: 1 }
		)
	);

	replaceChildren(domRefs.activation, grid);
}
