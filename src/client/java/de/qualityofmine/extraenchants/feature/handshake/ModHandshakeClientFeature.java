package de.qualityofmine.extraenchants.feature.handshake;

import de.qualityofmine.extraenchants.Feature;
import de.qualityofmine.extraenchants.ExtraEnchants;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;

/** Beantwortet die Versionsabfrage des Servers, ohne die er den Client nicht einlässt. */
public final class ModHandshakeClientFeature implements Feature {
	@Override
	public void initialize() {
		ClientConfigurationNetworking.registerGlobalReceiver(ModVersionPayload.TYPE, (payload, context) ->
			context.responseSender().sendPacket(new ModVersionReplyPayload(ExtraEnchants.version())));
	}
}
