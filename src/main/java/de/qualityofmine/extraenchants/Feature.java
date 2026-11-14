package de.qualityofmine.extraenchants;

/**
 * Ein abgeschlossenes Feature des Mods. Neue Verbesserungen bekommen ein eigenes Paket
 * unter {@code feature} und werden in {@link ExtraEnchants} eingetragen.
 */
public interface Feature {
	void initialize();
}
