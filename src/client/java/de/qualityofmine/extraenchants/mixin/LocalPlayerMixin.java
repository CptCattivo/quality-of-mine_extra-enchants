package de.qualityofmine.extraenchants.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.qualityofmine.extraenchants.feature.doublejump.DoubleJumpClient;
import de.qualityofmine.extraenchants.feature.elytraarmor.ElytraArmorClientFeature;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin {
	@Inject(method = "aiStep", at = @At("HEAD"))
	private void extraenchants$resetAirJump(final CallbackInfo ci) {
		DoubleJumpClient.tick((LocalPlayer) (Object) this);
		ElytraArmorClientFeature.tick((LocalPlayer) (Object) this);
	}

	/**
	 * Das Spiel versucht genau dann, die Elytren zu öffnen, wenn die Sprungtaste frisch gedrückt wurde
	 * und der Spieler an keiner Leiter hängt. Dieselbe Stelle löst im Flug den Raketenschub der Elytra-Rüstung aus
	 * und sonst den Doppelsprung.
	 */
	@WrapOperation(
		method = "aiStep",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;tryToStartFallFlying()Z")
	)
	private boolean extraenchants$jumpInAir(final LocalPlayer player, final Operation<Boolean> original) {
		if (ElytraArmorClientFeature.tryRocketBoost(player)) {
			return false;
		}
		return !DoubleJumpClient.tryAirJump(player) && original.call(player);
	}
}
