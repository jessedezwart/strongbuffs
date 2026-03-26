import type {
	ActionMeta,
	ConditionMeta,
	Manifest,
	ManifestRefs,
	SelectOption
} from "./types";

declare global
{
	interface Window
	{
		STRONGBUFFS_MANIFEST?: Manifest;
	}
}

function toMap<T>(items: T[], getKey: (item: T) => string): Record<string, T>
{
	return items.reduce(function (accumulator, item)
	{
		accumulator[getKey(item)] = item;
		return accumulator;
	}, {} as Record<string, T>);
}

export function getManifest(): Manifest | null
{
	return window.STRONGBUFFS_MANIFEST || null;
}

export function createManifestRefs(manifest: Manifest): ManifestRefs
{
	return {
		manifest: manifest,
		conditionMap: toMap<ConditionMeta>(manifest.conditions, function (item)
		{
			return item.typeId;
		}),
		actionMap: toMap<ActionMeta>(manifest.actions, function (item)
		{
			return item.typeId;
		})
	};
}

export function manifestValueOptions(values: string[]): SelectOption<string>[]
{
	return values.map(function (value)
	{
		return { value: value, label: value };
	});
}

export function getActionTypeOptions(manifest: Manifest): SelectOption<string>[]
{
	return manifest.actions.map(function (item)
	{
		return { value: item.typeId, label: item.label };
	});
}
