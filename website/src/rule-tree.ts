import type { ConditionGroup, ConditionNode } from "./types";

export function hasLeafCondition(group: ConditionGroup | null | undefined): boolean
{
	if (!group)
	{
		return false;
	}

	return group.children.some(function (child)
	{
		return child.type !== "group" || hasLeafCondition(child as ConditionGroup);
	});
}

export function getNode(rootGroup: ConditionGroup, path: number[]): ConditionNode
{
	let current: ConditionNode = rootGroup;

	path.forEach(function (index)
	{
		current = (current as ConditionGroup).children[index];
	});

	return current;
}

export function removeNode(rootGroup: ConditionGroup, path: number[]): void
{
	const parentPath = path.slice(0, -1);
	const index = path[path.length - 1];
	const parent = parentPath.length === 0
		? rootGroup
		: getNode(rootGroup, parentPath) as ConditionGroup;

	parent.children.splice(index, 1);
}
