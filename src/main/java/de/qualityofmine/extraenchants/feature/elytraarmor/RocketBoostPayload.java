package de.qualityofmine.extraenchants.feature.elytraarmor;

import de.qualityofmine.extraenchants.ExtraEnchants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client an Server: Im Flug wurde die Sprungtaste gedrückt, die Elytra-Rüstung soll einen Schub geben. */
public record RocketBoostPayload() implements CustomPacketPayload {
	public static final Type<RocketBoostPayload> TYPE = new Type<>(ExtraEnchants.id("rocket_boost"));
	public static final StreamCodec<ByteBuf, RocketBoostPayload> STREAM_CODEC = StreamCodec.unit(new RocketBoostPayload());

	@Override
	public Type<RocketBoostPayload> type() {
		return TYPE;
	}
}
