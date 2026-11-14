package de.qualityofmine.extraenchants;

import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import de.qualityofmine.extraenchants.feature.handshake.ModHandshakeFeature;
import de.qualityofmine.extraenchants.feature.nightvision.NightVisionFeature;

/**
 * Extra Enchants: Fünf neue Verzauberungen. Gehört zur Mod-Reihe „Quality of Mine“, die jedes Feature als eigene Mod anbietet.
 * Alle Inhalte stehen im Namensraum {@code qualityofmine}, damit bestehende Welten ihre Daten behalten.
 */
public final class ExtraEnchants implements ModInitializer {
	public static final String MOD_ID = "extraenchants";
	/** Der Namensraum aller Kennungen, Texte und Dateien. Er ist für die ganze Reihe derselbe. */
	public static final String NAMESPACE = "qualityofmine";

	public static Identifier id(final String path) {
		return Identifier.fromNamespaceAndPath(NAMESPACE, path);
	}

	/** Die Version aus fabric.mod.json, also die aus gradle.properties. */
	public static String version() {
		return FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
	}

	@Override
	public void onInitialize() {
		List<Feature> features = List.of(
			new ModHandshakeFeature(),
			new NightVisionFeature()
		);
		features.forEach(Feature::initialize);
	}
}
