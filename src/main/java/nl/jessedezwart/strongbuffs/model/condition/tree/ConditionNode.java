package nl.jessedezwart.strongbuffs.model.condition.tree;

/**
 * Common interface for persisted nodes inside a condition tree.
 *
 * <p>Leaf condition definitions and nested groups share the same persistence adapter, so both
 * expose a type id used during JSON serialization.</p>
 */
public interface ConditionNode
{
	String getTypeId();
}
