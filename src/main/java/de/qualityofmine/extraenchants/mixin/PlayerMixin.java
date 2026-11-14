package de.qualityofmine.extraenchants.mixin;

import de.qualityofmine.extraenchants.feature.healing.Healing;
import de.qualityofmine.extraenchants.feature.nightvision.NightVision;
import de.qualityofmine.extraenchants.feature.swiftness.Swiftness;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class PlayerMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	private void extraenchants$updateSwiftness(final CallbackInfo ci) {
		Swiftness.update((Player) (Object) this);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void extraenchants$heal(final CallbackInfo ci) {
		Healing.tick((Player) (Object) this);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void extraenchants$nightVision(final CallbackInfo ci) {
		NightVision.tick((Player) (Object) this);
	}
}
