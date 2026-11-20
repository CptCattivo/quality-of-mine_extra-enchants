package de.qualityofmine.extraenchants.feature.elytraarmor;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;

/**
 * Die Rakete hinter einem Schub der Elytra-Rüstung. Sie ist stärker und brennt länger als eine Rakete in der Hand.
 * Erkannt wird sie an der Komponente {@link ElytraArmors#ARMOR_ROCKET}, die mit dem Raketen-Item auch beim Client ankommt.
 */
public final class ArmorRocket {
	/** 20 % mehr Schub als eine Rakete aus der Hand, gern anpassen. */
	public static final double SPEED_FACTOR = 1.2;
	/**
	 * Eine Rakete brennt 10 Ticks je Stufe der Flugdauer plus 10 bis 21 Ticks. Stufe 1 aus der Hand sind also 1 bis 1,5 Sekunden,
	 * Stufe 11 gibt 5 Sekunden mehr.
	 */
	public static final int FLIGHT_DURATION = 11;

	private ArmorRocket() {
	}

	/** Ohne Feuerwerksstern, damit die Rakete beim Abbrennen keinen Schaden macht. */
	public static ItemStack createStack() {
		ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
		stack.set(DataComponents.FIREWORKS, new Fireworks(FLIGHT_DURATION, List.of()));
		stack.set(ElytraArmors.ARMOR_ROCKET, Unit.INSTANCE);
		return stack;
	}

	/** Verstärkt die Werte, mit denen eine angehängte Rakete den Spieler in Blickrichtung zieht. */
	public static double power(final double vanillaValue, final ItemStack rocket) {
		return rocket.has(ElytraArmors.ARMOR_ROCKET) ? scale(vanillaValue) : vanillaValue;
	}

	static double scale(final double vanillaValue) {
		return vanillaValue * SPEED_FACTOR;
	}
}
