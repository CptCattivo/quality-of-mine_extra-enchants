package de.qualityofmine.extraenchants.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import de.qualityofmine.extraenchants.ModEnchantments;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/**
 * Spielt Extra Enchants im echten Spiel durch: Klicks und Tasten gehen über die normale Eingabe des Clients.
 * Start mit {@code gradlew runClientGameTest}, Screenshots landen unter {@code build/run/clientGameTest/screenshots}.
 */
public final class ExtraEnchantsClientGameTest implements FabricClientGameTest {
		private static final Logger LOGGER = LogUtils.getLogger();
	private static final int HOTBAR_START = 36;
	private static final int TRASH_START = 46;
	private static final int TRASH_BIN = 49;
	/** Ein Punkt auf dem Panel neben dem Symbol. */
	private static final int PANEL_EDGE_X = 152;
	private static final int PANEL_EDGE_Y = 186;
	/** Mitte des Buttons „Nacht überspringen“, gemessen vom unteren Bildschirmrand. */
	private static final int SKIP_BUTTON_BOTTOM_OFFSET = 54;
	private static final int FIRST_EXTRA_SLOT = 9;
	private static final int LAST_EXTRA_SLOT = 10;
	private static final int MAX_TICKS_AFTER_SKIP = 100;
	private static final int IMAGE_WIDTH = 176;
	private static final int IMAGE_HEIGHT = 166;
	private static final double WALK_SPEED = 0.1;
	private static final double SPEED_TOLERANCE = 0.0001;
	/** Im Test laufen Client und Server im Gleichschritt. Eine kurze Restnacht reicht und spart Minuten. */
	private static final int LATE_NIGHT = 20000;
	/** Rund 3360 Ticks bis zum Wecken bei 200 Ticks pro Kartoffel, also knapp 17 Stück. */
	private static final int MIN_SMELTED_ITEMS = 15;
	private static final int EARLIEST_WAKE_UP = 23000;
	private static final int BREEDING_TIMEOUT_TICKS = 1200;
	private static final int REST_TIMEOUT_TICKS = 700;
	private static final int LURE_TICKS = 200;
	/** Länger als die Laufzeit des Effekts der Verzauberung, ohne Auffrischen wäre er dann weg. */
	private static final int NIGHT_VISION_REFRESH_CHECK = 450;

	@Override
	public void runTest(final ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			TestServerContext server = world.getServer();
			server.runCommand("gamerule advance_time true");
			server.runCommand("gamerule spawn_mobs true");
			server.runCommand("gamerule spawn_monsters false");
			server.runCommand("gamerule show_advancement_messages false");
			// Sonst heilt der volle Hungerbalken schneller als die Verzauberung
			server.runCommand("gamerule natural_health_regeneration false");
			server.runCommand("gamemode survival @a");
			context.waitTicks(20);

			testEnchantmentData(server);
			testSwiftness(context, server);
			testDoubleJump(context, server);
			testHealing(context, server);
			testNightVision(context, server);
			testSaturation(context, server);
		}
	}

	private static void testEnchantmentData(final TestServerContext server) {
		server.runOnServer(s -> {
			var enchantments = s.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			Holder.Reference<Enchantment> swiftness = enchantments.getOrThrow(ModEnchantments.SWIFTNESS);
			Holder.Reference<Enchantment> healing = enchantments.getOrThrow(ModEnchantments.HEALING);
			Holder.Reference<Enchantment> doubleJump = enchantments.getOrThrow(ModEnchantments.DOUBLE_JUMP);
			Holder.Reference<Enchantment> nightVision = enchantments.getOrThrow(ModEnchantments.NIGHT_VISION);
			Holder.Reference<Enchantment> saturation = enchantments.getOrThrow(ModEnchantments.SATURATION);

			check(swiftness.value().getMaxLevel() == 3, "Schnelligkeit hat die Stufen I bis III");
			check(swiftness.value().getWeight() == 2, "Schnelligkeit hat Gewicht 2");
			check(swiftness.value().canEnchant(new ItemStack(Items.DIAMOND_LEGGINGS)), "Schnelligkeit passt auf Hosen");
			check(!swiftness.value().canEnchant(new ItemStack(Items.DIAMOND_BOOTS)), "Schnelligkeit passt nicht auf Schuhe");
			check(healing.value().getMaxLevel() == 3, "Heilung hat die Stufen I bis III");
			check(healing.value().getWeight() == 2, "Heilung hat Gewicht 2 wie Schnelligkeit");
			check(healing.value().canEnchant(new ItemStack(Items.DIAMOND_CHESTPLATE)), "Heilung passt auf den Harnisch");
			check(!healing.value().canEnchant(new ItemStack(Items.DIAMOND_BOOTS)), "Heilung passt nicht auf Schuhe");
			check(doubleJump.value().getMaxLevel() == 1, "Doppelsprung hat nur eine Stufe");
			check(doubleJump.value().canEnchant(new ItemStack(Items.DIAMOND_BOOTS)), "Doppelsprung passt auf Schuhe");
			check(nightVision.value().getMaxLevel() == 1, "Nachtsicht hat nur eine Stufe");
			check(nightVision.value().canEnchant(new ItemStack(Items.DIAMOND_HELMET)), "Nachtsicht passt auf den Helm");
			check(saturation.value().getMaxLevel() == 3, "Sättigung hat die Stufen I bis III");
			check(saturation.value().canEnchant(new ItemStack(Items.DIAMOND_HELMET)), "Sättigung passt auf den Helm");
			check(nightVision.value().getWeight() == 2 && saturation.value().getWeight() == 2, "Nachtsicht und Sättigung haben Gewicht 2");
			for (Holder.Reference<Enchantment> enchantment : List.of(swiftness, healing, doubleJump, nightVision, saturation)) {
				for (TagKey<Enchantment> tag : List.of(EnchantmentTags.IN_ENCHANTING_TABLE, EnchantmentTags.ON_RANDOM_LOOT, EnchantmentTags.TRADEABLE)) {
					check(enchantment.is(tag), enchantment.key().identifier() + " ist im Tag " + tag.location());
				}
			}

			// Echte Häufigkeit am Zaubertisch auf Stufe 30, zum Vergleich mit Verzauberungen des Originalspiels
			List<Holder<Enchantment>> pool = enchantments.getOrThrow(EnchantmentTags.IN_ENCHANTING_TABLE).stream()
				.map(holder -> (Holder<Enchantment>) holder).toList();
			Map<Integer, Integer> levels = logFrequency(pool, new ItemStack(Items.DIAMOND_LEGGINGS), swiftness);
			logFrequency(pool, new ItemStack(Items.DIAMOND_BOOTS), enchantments.getOrThrow(Enchantments.DEPTH_STRIDER));
			logFrequency(pool, new ItemStack(Items.DIAMOND_BOOTS), enchantments.getOrThrow(Enchantments.THORNS));
			logFrequency(pool, new ItemStack(Items.DIAMOND_PICKAXE), enchantments.getOrThrow(Enchantments.SILK_TOUCH));
			check(!levels.isEmpty(), "Schnelligkeit erscheint am Zaubertisch");
			check(!logFrequency(pool, new ItemStack(Items.DIAMOND_CHESTPLATE), healing).isEmpty(), "Heilung erscheint am Zaubertisch");
			check(!logFrequency(pool, new ItemStack(Items.DIAMOND_BOOTS), doubleJump).isEmpty(), "Doppelsprung erscheint am Zaubertisch");
			check(!logFrequency(pool, new ItemStack(Items.DIAMOND_HELMET), nightVision).isEmpty(), "Nachtsicht erscheint am Zaubertisch");
			check(!logFrequency(pool, new ItemStack(Items.DIAMOND_HELMET), saturation).isEmpty(), "Sättigung erscheint am Zaubertisch");
		});
	}

	private static Map<Integer, Integer> logFrequency(final List<Holder<Enchantment>> pool, final ItemStack stack, final Holder.Reference<Enchantment> target) {
		RandomSource random = RandomSource.create(1234L);
		Map<Integer, Integer> levels = new HashMap<>();
		int rolls = 100000;
		for (int i = 0; i < rolls; i++) {
			for (EnchantmentInstance instance : EnchantmentHelper.selectEnchantment(random, stack, 30, pool.stream())) {
				if (instance.enchantment().equals(target)) {
					levels.merge(instance.level(), 1, Integer::sum);
				}
			}
		}
		LOGGER.info("[QoM-Test] Zaubertisch Stufe 30, {}, {} Versuche: {} auf den Stufen {}", stack.getItem(), rolls, target.key().identifier(), levels);
		return levels;
	}

	private static double clientSpeed(final ClientGameTestContext context) {
		return context.computeOnClient(m -> m.player.getAttributeValue(Attributes.MOVEMENT_SPEED));
	}

	private static double serverSpeed(final TestServerContext server) {
		return server.computeOnServer(s -> player(s).getAttributeValue(Attributes.MOVEMENT_SPEED));
	}

	private static void measureLevel(final ClientGameTestContext context, final TestServerContext server, final int level) {
		String enchantment = level == 0 ? "" : "[enchantments={\"qualityofmine:swiftness\":" + level + "}]";
		server.runCommand("item replace entity @a armor.legs with minecraft:diamond_leggings" + enchantment);
		context.waitTicks(5);

		context.getInput().holdKey(options -> options.keyUp);
		context.waitTicks(10);
		double expectedWalk = WALK_SPEED;
		check(Math.abs(clientSpeed(context) - expectedWalk) < SPEED_TOLERANCE, "Stufe " + level + ": Gehen bleibt bei " + expectedWalk);

		context.getInput().holdKey(options -> options.keySprint);
		context.waitTicks(10);
		double expectedSprint = WALK_SPEED * 1.3 * (1.0 + level * 0.1);
		check(context.computeOnClient(m -> m.player.isSprinting()), "Stufe " + level + ": Spieler sprintet");
		check(Math.abs(clientSpeed(context) - expectedSprint) < SPEED_TOLERANCE, "Stufe " + level + ": Sprint auf dem Client " + expectedSprint);
		check(Math.abs(serverSpeed(server) - expectedSprint) < SPEED_TOLERANCE, "Stufe " + level + ": Sprint auf dem Server " + expectedSprint);

		context.getInput().releaseKey(options -> options.keySprint);
		context.getInput().releaseKey(options -> options.keyUp);
		context.waitTicks(5);
		context.runOnClient(minecraft -> minecraft.player.setSprinting(false));
		context.waitTicks(5);
		check(Math.abs(clientSpeed(context) - WALK_SPEED) < SPEED_TOLERANCE, "Stufe " + level + ": Nach dem Sprint ist der Bonus weg");
	}

	private static void testSwiftness(final ClientGameTestContext context, final TestServerContext server) {
		for (int level = 0; level <= 3; level++) {
			measureLevel(context, server, level);
		}
		server.runCommand("item replace entity @a armor.legs with minecraft:air");
	}

	/** Springt über die echte Sprungtaste und misst, wie hoch der Spieler kommt. */
	private static double jumpHeight(final ClientGameTestContext context, final int presses) {
		context.waitFor(minecraft -> minecraft.player.onGround());
		double startY = context.computeOnClient(m -> m.player.getY());
		double maxY = startY;
		for (int press = 0; press < presses; press++) {
			// Die Taste muss einen ganzen Tick gedrückt sein, damit die Steuerung sie sieht
			context.getInput().holdKey(options -> options.keyJump);
			context.waitTick();
			context.getInput().releaseKey(options -> options.keyJump);
			// Kurz vor dem höchsten Punkt des vorigen Sprungs erneut drücken
			for (int tick = 0; tick < 5; tick++) {
				context.waitTick();
				maxY = Math.max(maxY, context.computeOnClient(m -> m.player.getY()));
			}
		}
		for (int tick = 0; tick < 30; tick++) {
			context.waitTick();
			maxY = Math.max(maxY, context.computeOnClient(m -> m.player.getY()));
		}
		context.waitFor(minecraft -> minecraft.player.onGround());
		LOGGER.info("[QoM-Test] Sprunghöhe bei {} Mal Drücken: {}", presses, maxY - startY);
		return maxY - startY;
	}

	private static void testDoubleJump(final ClientGameTestContext context, final TestServerContext server) {
		server.runCommand("item replace entity @a armor.feet with minecraft:diamond_boots");
		context.waitTicks(5);
		check(jumpHeight(context, 2) < 1.3, "Ohne Verzauberung gibt es keinen Sprung in der Luft");

		server.runCommand("item replace entity @a armor.feet with minecraft:diamond_boots[enchantments={\"qualityofmine:double_jump\":1}]");
		context.waitTicks(5);
		check(jumpHeight(context, 1) < 1.3, "Ein einfacher Sprung bleibt normal hoch");
		check(jumpHeight(context, 2) > 2.0, "Mit Doppelsprung kommt der Spieler über 2 Blöcke");
		check(jumpHeight(context, 3) < 2.6, "In der Luft gibt es nur einen zusätzlichen Sprung");
		check(server.computeOnServer(s -> player(s).getHealth() == player(s).getMaxHealth()), "Der Doppelsprung verursacht keinen Fallschaden");
		server.runCommand("item replace entity @a armor.feet with minecraft:air");
	}

	private static void testHealing(final ClientGameTestContext context, final TestServerContext server) {
		server.runCommand("item replace entity @a armor.chest with minecraft:diamond_chestplate[enchantments={\"qualityofmine:healing\":3}]");
		server.runOnServer(s -> player(s).setHealth(10.0F));
		context.waitTicks(401);
		check(server.computeOnServer(s -> player(s).getHealth()) == 11.0F, "Heilung III gibt in 20 Sekunden ein halbes Herz zurück");
		server.runCommand("item replace entity @a armor.chest with minecraft:air");
		server.runOnServer(s -> player(s).setHealth(player(s).getMaxHealth()));
	}

	private static boolean clientHasNightVision(final ClientGameTestContext context) {
		return context.computeOnClient(m -> m.player.hasEffect(MobEffects.NIGHT_VISION));
	}

	private static void testNightVision(final ClientGameTestContext context, final TestServerContext server) {
		server.runCommand("effect clear @a");
		server.runCommand("item replace entity @a armor.head with minecraft:diamond_helmet[enchantments={\"qualityofmine:night_vision\":1}]");
		context.waitTicks(5);
		check(clientHasNightVision(context), "Der Helm gibt Nachtsicht");
		check(context.computeOnClient(m -> !m.player.getEffect(MobEffects.NIGHT_VISION).isVisible()), "Die Nachtsicht zeigt keine Partikel");

		context.waitTicks(NIGHT_VISION_REFRESH_CHECK);
		check(context.computeOnClient(m -> m.player.getEffect(MobEffects.NIGHT_VISION).getDuration()) > 200, "Die Nachtsicht wird vor dem Flackern aufgefrischt");

		context.getInput().pressKey(InputConstants.KEY_N);
		context.waitTicks(5);
		check(!clientHasNightVision(context), "Taste N schaltet die Nachtsicht aus");
		context.getInput().pressKey(InputConstants.KEY_N);
		context.waitTicks(5);
		check(clientHasNightVision(context), "Taste N schaltet die Nachtsicht wieder ein");

		// Ein Trank gehört nicht der Verzauberung und bleibt beim Ausschalten erhalten
		server.runCommand("effect give @a minecraft:night_vision 60");
		context.waitTicks(5);
		context.getInput().pressKey(InputConstants.KEY_N);
		context.waitTicks(5);
		check(clientHasNightVision(context), "Ausschalten lässt einen Trank weiterlaufen");
		context.getInput().pressKey(InputConstants.KEY_N);
		server.runCommand("effect clear @a");
		context.waitTicks(5);

		server.runCommand("item replace entity @a armor.head with minecraft:air");
		context.waitTicks(5);
		check(!clientHasNightVision(context), "Ohne Helm endet die Nachtsicht");
	}

	/** Lädt die Erschöpfung auf das Maximum von 40 und zählt, wie viele Sättigungspunkte dabei verloren gehen. */
	private static float saturationLost(final ClientGameTestContext context, final TestServerContext server, final int level) {
		String enchantment = level == 0 ? "" : "[enchantments={\"qualityofmine:saturation\":" + level + "}]";
		server.runCommand("item replace entity @a armor.head with minecraft:diamond_helmet" + enchantment);
		server.runOnServer(s -> {
			player(s).getFoodData().setFoodLevel(20);
			player(s).getFoodData().setSaturation(20.0F);
			player(s).getFoodData().addExhaustion(40.0F);
		});
		context.waitTicks(20);
		float lost = 20.0F - server.computeOnServer(s -> player(s).getFoodData().getSaturationLevel());
		LOGGER.info("[QoM-Test] Sättigung Stufe {}: {} Punkte verloren", level, lost);
		return lost;
	}

	private static void testSaturation(final ClientGameTestContext context, final TestServerContext server) {
		// Je Punkt braucht es mehr als 4, 4,44, 5,33 bzw. 8 Erschöpfung: Aus 40 werden 9, 8, 7 bzw. 4 Punkte
		check(saturationLost(context, server, 0) == 9.0F, "Ohne Verzauberung kosten 40 Erschöpfung 9 Punkte");
		check(saturationLost(context, server, 1) == 8.0F, "Sättigung I spart 10 Prozent");
		check(saturationLost(context, server, 2) == 7.0F, "Sättigung II spart 25 Prozent");
		check(saturationLost(context, server, 3) == 4.0F, "Sättigung III halbiert den Verbrauch");
		server.runCommand("item replace entity @a armor.head with minecraft:air");
	}

	static ServerPlayer player(final MinecraftServer server) {
		return server.getPlayerList().getPlayers().getFirst();
	}

	static void check(final boolean condition, final String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
		LOGGER.info("[QoM-Test] OK: {}", message);
	}
}
