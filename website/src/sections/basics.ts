import { replaceChildren, createFieldGrid } from "../dom";
import { createTextField, createCheckboxField } from "../fields";
import type { DomRefs, RuleState } from "../types";

interface BasicActions {
  updateRuleField: (key: keyof RuleState, value: unknown) => void;
}

export function renderBasicsSection(
  domRefs: DomRefs,
  rule: RuleState,
  actions: BasicActions,
): void {
  const grid = createFieldGrid();

  grid.appendChild(
    createTextField(
      "Rule name",
      rule.name,
      function (value) {
        actions.updateRuleField("name", value);
      },
      { placeholder: "Low HP Warning" },
    ),
  );

  grid.appendChild(
    createCheckboxField("Enabled", rule.enabled, function (value) {
      actions.updateRuleField("enabled", value);
    }),
  );

  replaceChildren(domRefs.basics, grid);
}
