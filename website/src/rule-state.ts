import type {
  ActionMeta,
  DefinitionNode,
  Manifest,
  ManifestRefs,
  ManifestField,
  RuleState,
} from "./types";

export function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}

function resolveFieldDefault(
  definition: DefinitionNode,
  field: ManifestField,
): unknown {
  if (definition[field.key] !== undefined) {
    return definition[field.key];
  }

  if (field.defaultValue !== undefined) {
    return clone(field.defaultValue);
  }

  if (field.kind === "spinner" || field.kind === "slider") {
    return field.minimumValue ?? 0;
  }

  if (field.kind === "checkbox") {
    return false;
  }

  return "";
}

export function createDefinitionFromManifest(
  meta: ActionMeta | { defaults: DefinitionNode; fields: ManifestField[] },
): DefinitionNode {
  const definition = clone(meta.defaults);

  meta.fields.forEach(function (field) {
    if (definition[field.key] !== undefined) {
      return;
    }

    definition[field.key] = resolveFieldDefault(definition, field);
  });

  return definition;
}

export function createRuleState(manifestRefs: ManifestRefs): RuleState {
  const manifest = manifestRefs.manifest;

  return {
    schemaVersion: manifest.schemaVersion,
    name: "",
    enabled: true,
    rootGroup: clone(manifest.groupDefaults),
    activationMode: String(manifest.activationModes[0].value),
    cooldownTicks: 0,
    action: createDefinitionFromManifest(
      manifestRefs.actionMap[manifest.defaultActionType],
    ),
  };
}

export function createExportedRule(
  rule: RuleState,
  manifest: Manifest,
): RuleState {
  return {
    schemaVersion: manifest.schemaVersion,
    name: rule.name.trim(),
    enabled: rule.enabled,
    rootGroup: clone(rule.rootGroup),
    activationMode: rule.activationMode,
    cooldownTicks: rule.cooldownTicks,
    action: clone(rule.action),
  };
}
