/**
 * Cached runtime state slices used for rule evaluation.
 *
 * <p>The engine evaluates rules from these snapshots instead of reading RuneLite APIs ad hoc,
 * which keeps evaluation deterministic and lets tracking stay selective.</p>
 */
package nl.jessedezwart.strongbuffs.runtime.state;
