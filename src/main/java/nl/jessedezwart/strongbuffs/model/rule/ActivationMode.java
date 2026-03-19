package nl.jessedezwart.strongbuffs.model.rule;

/**
 * Defines whether a rule acts continuously while matched or only on match transitions.
 */
public enum ActivationMode
{
	WHILE_ACTIVE,
	ON_ENTER,
	ON_EXIT
}
