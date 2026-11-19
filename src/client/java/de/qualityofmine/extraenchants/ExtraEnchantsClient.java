package de.qualityofmine.extraenchants;

import de.qualityofmine.extraenchants.Feature;
import de.qualityofmine.extraenchants.feature.elytraarmor.ElytraArmorClientFeature;
import de.qualityofmine.extraenchants.feature.handshake.ModHandshakeClientFeature;
import de.qualityofmine.extraenchants.feature.nightvision.NightVisionClientFeature;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;

public final class ExtraEnchantsClient implements ClientModInitializer {
	/** Darf nur einmal registriert werden und gilt für alle Tasten des Mods. */
	public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(ExtraEnchants.id("extraenchants"));

	@Override
	public void onInitializeClient() {
		List<Feature> features = List.of(
			new ModHandshakeClientFeature(),
			new NightVisionClientFeature(),
			new ElytraArmorClientFeature()
		);
		features.forEach(Feature::initialize);
	}
}
