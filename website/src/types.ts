export type OptionValue = string | number | boolean;

export interface SelectOption<T = OptionValue> {
  value: T;
  label: string;
}

export type ManifestFieldKind =
  | "text"
  | "color"
  | "checkbox"
  | "choice"
  | "spinner"
  | "slider";

export interface ManifestField {
  key: string;
  kind: ManifestFieldKind;
  label?: string;
  defaultValue?: unknown;
  minimumValue?: number;
  maximumValue?: number;
  stepSize?: number;
  suffix?: string;
  options?: SelectOption[];
}

export interface DefinitionNode {
  type: string;
  [key: string]: unknown;
}

export interface ConditionGroup {
  type: "group";
  logic: string;
  children: ConditionNode[];
}

export type ConditionNode = ConditionGroup | DefinitionNode;

export interface ConditionMeta {
  typeId: string;
  label: string;
  defaults: DefinitionNode;
  fields: ManifestField[];
}

export interface ActionMeta {
  typeId: string;
  label: string;
  defaults: DefinitionNode;
  fields: ManifestField[];
}

export interface Manifest {
  schemaVersion: number;
  conditions: ConditionMeta[];
  actions: ActionMeta[];
  activationModes: SelectOption[];
  defaultActionType: string;
  groupDefaults: ConditionGroup;
}

export interface RuleState {
  schemaVersion: number;
  name: string;
  enabled: boolean;
  rootGroup: ConditionGroup;
  activationMode: string;
  cooldownTicks: number;
  action: DefinitionNode;
}

export interface ValidationResult {
  isValid: boolean;
  errors: string[];
}

export interface FocusState {
  focusId: string;
  selectionStart: number | null;
  selectionEnd: number | null;
}

export interface ManifestRefs {
  manifest: Manifest;
  conditionMap: Record<string, ConditionMeta>;
  actionMap: Record<string, ActionMeta>;
}

export interface DomRefs {
  basics: HTMLElement;
  conditions: HTMLElement;
  activation: HTMLElement;
  action: HTMLElement;
  status: HTMLElement;
  json: HTMLElement;
  copy: HTMLButtonElement;
  download: HTMLButtonElement;
}
