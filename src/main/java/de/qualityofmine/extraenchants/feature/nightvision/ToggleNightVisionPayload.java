package de.qualityofmine.extraenchants.feature.nightvision;

import de.qualityofmine.extraenchants.ExtraEnchants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client an Server: Die Taste für Nachtsicht wurde gedrückt. */
public record ToggleNightVisionPayload() implements CustomPacketPayload {
	public static final Type<ToggleNightVisionPayload> TYPE = new Type<>(ExtraEnchants.id("toggle_night_vision"));
	public static final StreamCodec<ByteBuf, ToggleNightVisionPayload> STREAM_CODEC = StreamCodec.unit(new ToggleNightVisionPayload());

	@Override
	public Type<ToggleNightVisionPayload> type() {
		return TYPE;
	}
}
