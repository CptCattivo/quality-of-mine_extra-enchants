package de.qualityofmine.extraenchants.feature.elytraarmor;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Harnisch mit Elytren. Feuerwerksraketen lädt man wie beim Bündel: Rakete im Inventar aufnehmen und auf die Rüstung klicken.
 * Ein Rechtsklick legt eine Rakete hinein, ein Linksklick so viele, wie passen.
 */
public class ElytraArmorItem extends Item {
	public ElytraArmorItem(final Properties properties) {
		super(properties);
	}

	@Override
	public boolean overrideOtherStackedOnMe(final ItemStack self, final ItemStack other, final Slot slot, final ClickAction clickAction,
		final Player player, final SlotAccess carriedItem) {
		if (!other.is(Items.FIREWORK_ROCKET)) {
			return false;
		}
		int offered = clickAction == ClickAction.SECONDARY ? 1 : other.getCount();
		int charge = ElytraArmors.chargeOf(self);
		int rockets = slot.allowModification(player) ? RocketCharge.rocketsThatFit(charge, offered) : 0;
		// Auch eine volle Rüstung schluckt den Klick, sonst würde das Spiel Rakete und Rüstung vertauschen
		if (rockets == 0) {
			player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
			return true;
		}
		ElytraArmors.setCharge(self, RocketCharge.load(charge, rockets));
		other.shrink(rockets);
		player.playSound(SoundEvents.CROSSBOW_LOADING_END.value(), 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
		return true;
	}

	@Override
	public void appendHoverText(final ItemStack stack, final TooltipContext context, final TooltipDisplay display,
		final Consumer<Component> builder, final TooltipFlag tooltipFlag) {
		int charge = ElytraArmors.chargeOf(stack);
		if (charge > 0) {
			builder.accept(Component.translatable("item.qualityofmine.elytra_armor.charge", charge, RocketCharge.MAX_BOOSTS)
				.withStyle(ChatFormatting.GOLD));
		} else {
			builder.accept(Component.translatable("item.qualityofmine.elytra_armor.empty").withStyle(ChatFormatting.GRAY));
		}
	}
}
