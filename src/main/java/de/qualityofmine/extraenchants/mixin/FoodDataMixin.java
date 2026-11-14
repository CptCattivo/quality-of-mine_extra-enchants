package de.qualityofmine.extraenchants.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.qualityofmine.extraenchants.feature.saturation.Saturation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodData.class)
abstract class FoodDataMixin {
	/** Die Schwelle 4.0 steht in tick zweimal: beim Vergleich mit der Erschöpfung und beim Abziehen. Beide ändern sich gleich. */
	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "floatValue=4.0F"))
	private float extraenchants$saturation(final float exhaustionPerPoint, @Local(argsOnly = true) final ServerPlayer player) {
		return Saturation.exhaustionPerPoint(player, exhaustionPerPoint);
	}
}
