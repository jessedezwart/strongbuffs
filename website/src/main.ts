import { beginRender, captureFocusState, restoreFocusState } from "./dom";
import { getManifest, createManifestRefs } from "./manifest";
import {
  clone,
  createDefinitionFromManifest,
  createExportedRule,
  createRuleState,
} from "./rule-state";
import { getNode, removeNode } from "./rule-tree";
import { validateRule } from "./validation";
import { copyJson, downloadJson } from "./browser-actions";
import { renderBasicsSection } from "./sections/basics";
import { renderConditionsSection } from "./sections/conditions";
import { renderActivationSection } from "./sections/activation";
import { renderActionSection } from "./sections/action";
import { renderOutputSection } from "./sections/output";
import type {
  ConditionGroup,
  DefinitionNode,
  DomRefs,
  FocusState,
  ManifestRefs,
  RuleState,
  ValidationResult,
} from "./types";

interface AppState {
  rule: RuleState;
  validation: ValidationResult;
  focus: FocusState | null;
}

interface AppActions {
  updateRuleField: (key: keyof RuleState, value: unknown) => void;
  updateActionField: (key: string, value: unknown) => void;
  changeActionType: (typeId: string) => void;
  updateConditionField: (path: number[], key: string, value: unknown) => void;
  updateGroupLogic: (path: number[], value: string) => void;
  addCondition: (group: ConditionGroup, typeId: string) => void;
  addGroup: (group: ConditionGroup) => void;
  removeNode: (path: number[]) => void;
}

function requireElement<T extends HTMLElement>(id: string): T {
  const element = document.getElementById(id);

  if (!(element instanceof HTMLElement)) {
    throw new Error("Missing DOM element: " + id);
  }

  return element as T;
}

function createDomRefs(): DomRefs {
  return {
    basics: requireElement("basics-section"),
    conditions: requireElement("conditions-section"),
    activation: requireElement("activation-section"),
    action: requireElement("action-section"),
    status: requireElement("status-box"),
    json: requireElement("json-output"),
    copy: requireElement("copy-json"),
    download: requireElement("download-json"),
  };
}

function createActions(
  state: AppState,
  manifestRefs: ManifestRefs,
  render: () => void,
  refreshOutput: () => void,
): AppActions {
  return {
    updateRuleField: function (key, value) {
      (state.rule[key] as unknown) = value;
      refreshOutput();
    },
    updateActionField: function (key, value) {
      state.rule.action[key] = value;
      refreshOutput();
    },
    changeActionType: function (typeId) {
      state.rule.action = createDefinitionFromManifest(
        manifestRefs.actionMap[typeId],
      );
      render();
    },
    updateConditionField: function (path, key, value) {
      (getNode(state.rule.rootGroup, path) as DefinitionNode)[key] = value;
      refreshOutput();
    },
    updateGroupLogic: function (path, value) {
      (getNode(state.rule.rootGroup, path) as ConditionGroup).logic = value;
      refreshOutput();
    },
    addCondition: function (group, typeId) {
      group.children.push(
        createDefinitionFromManifest(manifestRefs.conditionMap[typeId]),
      );
      render();
    },
    addGroup: function (group) {
      group.children.push(clone(manifestRefs.manifest.groupDefaults));
      render();
    },
    removeNode: function (path) {
      removeNode(state.rule.rootGroup, path);
      render();
    },
  };
}

function bootstrap(): void {
  const manifest = getManifest();

  if (!manifest) {
    console.error("Strong Buffs manifest is missing.");
    return;
  }

  const manifestRefs = createManifestRefs(manifest);
  const domRefs = createDomRefs();
  const rule = createRuleState(manifestRefs);
  const state: AppState = {
    rule: rule,
    validation: validateRule(rule, manifestRefs),
    focus: null,
  };

  function refreshOutput(): void {
    state.validation = validateRule(state.rule, manifestRefs);
    const exportedRule = createExportedRule(state.rule, manifestRefs.manifest);
    renderOutputSection(domRefs, exportedRule, state.validation);
  }

  const actions = createActions(state, manifestRefs, render, refreshOutput);

  domRefs.copy.addEventListener("click", function () {
    if (!state.validation.isValid) {
      return;
    }

    copyJson(domRefs.json.textContent || "", domRefs.json);
  });

  domRefs.download.addEventListener("click", function () {
    if (!state.validation.isValid) {
      return;
    }

    downloadJson(domRefs.json.textContent || "", state.rule.name);
  });

  function render(): void {
    state.focus = captureFocusState();
    beginRender();
    renderBasicsSection(domRefs, state.rule, actions);
    renderConditionsSection(domRefs, manifestRefs, state.rule, actions);
    renderActivationSection(
      domRefs,
      manifestRefs.manifest,
      state.rule,
      actions,
    );
    renderActionSection(domRefs, manifestRefs, state.rule, actions);
    refreshOutput();
    domRefs.basics.setAttribute("aria-busy", "false");
    domRefs.conditions.setAttribute("aria-busy", "false");
    domRefs.activation.setAttribute("aria-busy", "false");
    domRefs.action.setAttribute("aria-busy", "false");
    domRefs.status.setAttribute("aria-busy", "false");
    document.documentElement.classList.remove("app-loading");
    restoreFocusState(state.focus);
  }

  render();
}

bootstrap();
