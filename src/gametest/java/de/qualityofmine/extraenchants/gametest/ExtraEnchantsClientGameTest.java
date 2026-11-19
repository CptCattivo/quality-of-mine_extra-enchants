package de.qualityofmine.extraenchants.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import de.qualityofmine.extraenchants.ModEnchantments;
import de.qualityofmine.extraenchants.feature.elytraarmor.ElytraArmors;
import de.qualityofmine.extraenchants.feature.elytraarmor.RocketCharge;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
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
			testElytraArmor(context, server);
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
			// Unendlichkeit und Reparatur vertragen sich auf dem Bogen, andere Unverträglichkeiten bleiben bestehen
			Holder.Reference<Enchantment> infinity = enchantments.getOrThrow(Enchantments.INFINITY);
			Holder.Reference<Enchantment> mending = enchantments.getOrThrow(Enchantments.MENDING);
			check(Enchantment.areCompatible(infinity, mending), "Unendlichkeit und Reparatur sind kombinierbar");
			check(infinity.value().canEnchant(new ItemStack(Items.BOW)) && mending.value().canEnchant(new ItemStack(Items.BOW)), "Beide passen auf den Bogen");
			Holder.Reference<Enchantment> sharpness = enchantments.getOrThrow(Enchantments.SHARPNESS);
			Holder.Reference<Enchantment> smite = enchantments.getOrThrow(Enchantments.SMITE);
			check(!Enchantment.areCompatible(sharpness, smite), "Schärfe und Bann der Toten bleiben unverträglich");

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

	/** Legt Harnisch und Elytren in einen Amboss und nimmt das Ergebnis heraus, wie es ein Spieler tun würde. */
	private static ItemStack combineInAnvil(final MinecraftServer server, final ItemStack input, final ItemStack addition, final int levels) {
		ServerPlayer player = player(server);
		AnvilMenu anvil = new AnvilMenu(0, player.getInventory(), ContainerLevelAccess.NULL);
		anvil.getSlot(AnvilMenu.INPUT_SLOT).set(input);
		anvil.getSlot(AnvilMenu.ADDITIONAL_SLOT).set(addition);
		player.experienceLevel = levels;
		anvil.clicked(AnvilMenu.RESULT_SLOT, 0, ContainerInput.PICKUP, player);
		ItemStack taken = anvil.getCarried();
		anvil.setCarried(ItemStack.EMPTY);
		return taken;
	}

	private static void testElytraArmor(final ClientGameTestContext context, final TestServerContext server) {
		server.runOnServer(s -> {
			var enchantments = s.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			Holder.Reference<Enchantment> protection = enchantments.getOrThrow(Enchantments.PROTECTION);
			Holder.Reference<Enchantment> unbreaking = enchantments.getOrThrow(Enchantments.UNBREAKING);
			Holder.Reference<Enchantment> mending = enchantments.getOrThrow(Enchantments.MENDING);
			Holder.Reference<Enchantment> healing = enchantments.getOrThrow(ModEnchantments.HEALING);

			ItemStack armor = new ItemStack(ElytraArmors.ELYTRA_ARMOR);
			ItemStack netherite = new ItemStack(ElytraArmors.NETHERITE_ELYTRA_ARMOR);
			check(armor.getMaxDamage() == 528 + 432, "Die Elytra-Rüstung hält so viel aus wie Harnisch und Elytren zusammen");
			check(netherite.getMaxDamage() == 592 + 432, "Die Netherit-Elytra-Rüstung hält so viel aus wie Harnisch und Elytren zusammen");
			check(armor.has(DataComponents.GLIDER) && armor.is(ItemTags.CHEST_ARMOR), "Die Elytra-Rüstung gleitet und zählt als Harnisch");
			check(netherite.has(DataComponents.DAMAGE_RESISTANT), "Die Netherit-Elytra-Rüstung verbrennt nicht");
			check(armor.isValidRepairItem(new ItemStack(Items.DIAMOND)) && armor.isValidRepairItem(new ItemStack(Items.PHANTOM_MEMBRANE)),
				"Die Elytra-Rüstung lässt sich mit Diamanten und Phantomhaut reparieren");
			for (Holder.Reference<Enchantment> enchantment : List.of(protection, unbreaking, mending, healing)) {
				check(enchantment.value().canEnchant(armor), enchantment.key().identifier() + " passt auf die Elytra-Rüstung");
			}

			// Amboss: Die Verzauberungen beider Teile bleiben, der Verschleiß addiert sich
			ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
			chestplate.enchant(protection, 4);
			chestplate.enchant(healing, 2);
			chestplate.enchant(unbreaking, 2);
			chestplate.setDamageValue(100);
			ItemStack elytra = new ItemStack(Items.ELYTRA);
			elytra.enchant(unbreaking, 3);
			elytra.enchant(mending, 1);
			elytra.setDamageValue(50);
			ItemStack combined = combineInAnvil(s, chestplate.copy(), elytra.copy(), 10);
			check(combined.is(ElytraArmors.ELYTRA_ARMOR), "Harnisch und Elytren ergeben im Amboss die Elytra-Rüstung");
			check(combined.getDamageValue() == 150, "Der Verschleiß beider Teile bleibt erhalten");
			check(EnchantmentHelper.getItemEnchantmentLevel(protection, combined) == 4 && EnchantmentHelper.getItemEnchantmentLevel(healing, combined) == 2,
				"Die Verzauberungen des Harnischs bleiben erhalten");
			check(EnchantmentHelper.getItemEnchantmentLevel(unbreaking, combined) == 3 && EnchantmentHelper.getItemEnchantmentLevel(mending, combined) == 1,
				"Von den Elytren kommen Haltbarkeit III und Reparatur dazu");
			check(player(s).experienceLevel == 5, "Das Zusammenfügen kostet 5 Stufen");
			check(combineInAnvil(s, elytra.copy(), chestplate.copy(), 10).isEmpty(), "Elytren links und Harnisch rechts ergeben nichts");
			ItemStack expensive = new ItemStack(Items.DIAMOND_CHESTPLATE);
			expensive.set(DataComponents.REPAIR_COST, 39);
			check(combineInAnvil(s, expensive, new ItemStack(Items.ELYTRA), 100).isEmpty(), "Zu teure Ambossarbeit bleibt wie im Spiel gesperrt");
			player(s).experienceLevel = 0;
			player(s).getInventory().clearContent();

			// Schmiedetisch: Netherit-Aufwertung mit allen Komponenten
			SmithingRecipeInput smithing = new SmithingRecipeInput(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), combined,
				new ItemStack(Items.NETHERITE_INGOT));
			ItemStack upgraded = s.getRecipeManager().getRecipeFor(RecipeType.SMITHING, smithing, s.overworld(), (ResourceKey<Recipe<?>>) null)
				.map(recipe -> recipe.value().assemble(smithing)).orElse(ItemStack.EMPTY);
			check(upgraded.is(ElytraArmors.NETHERITE_ELYTRA_ARMOR), "Der Schmiedetisch macht daraus die Netherit-Elytra-Rüstung");
			check(EnchantmentHelper.getItemEnchantmentLevel(protection, upgraded) == 4, "Die Verzauberungen überstehen die Netherit-Aufwertung");

			// Laden wie beim Bündel: Rechtsklick legt eine Rakete hinein, Linksklick so viele wie passen
			ServerPlayer player = player(s);
			player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ElytraArmors.ELYTRA_ARMOR));
			int chestSlot = 6;
			var menu = player.inventoryMenu;
			menu.setCarried(new ItemStack(Items.FIREWORK_ROCKET, 5));
			menu.clicked(chestSlot, 1, ContainerInput.PICKUP, player);
			check(ElytraArmors.chargeOf(player.getItemBySlot(EquipmentSlot.CHEST)) == 48 && menu.getCarried().getCount() == 4,
				"Rechtsklick lädt eine Rakete für 48 Schübe");
			menu.clicked(chestSlot, 0, ContainerInput.PICKUP, player);
			check(ElytraArmors.chargeOf(player.getItemBySlot(EquipmentSlot.CHEST)) == 96 && menu.getCarried().getCount() == 3,
				"Linksklick füllt bis 2 Raketen auf");
			menu.clicked(chestSlot, 0, ContainerInput.PICKUP, player);
			check(player.getItemBySlot(EquipmentSlot.CHEST).is(ElytraArmors.ELYTRA_ARMOR) && menu.getCarried().getCount() == 3,
				"Eine volle Rüstung nimmt nichts mehr und bleibt angelegt");
			menu.setCarried(ItemStack.EMPTY);
		});
		// Die Attribute angelegter Ausrüstung übernimmt das Spiel erst im nächsten Tick
		context.waitTicks(2);
		check(server.computeOnServer(s -> player(s).getArmorValue()) == 8, "Die Elytra-Rüstung schützt wie ein Diamantharnisch");
		server.runCommand("item replace entity @a armor.chest with minecraft:air");

		testElytraArmorFlight(context, server);
		testElytraArmorInventory(context, server);
	}

	private static double horizontalSpeed(final ClientGameTestContext context) {
		return context.computeOnClient(m -> m.player.getDeltaMovement().horizontalDistance());
	}

	private static int rocketsNearPlayer(final TestServerContext server) {
		return server.computeOnServer(s -> player(s).level()
			.getEntitiesOfClass(FireworkRocketEntity.class, player(s).getBoundingBox().inflate(5.0)).size());
	}

	private static void pressJump(final ClientGameTestContext context) {
		context.getInput().holdKey(options -> options.keyJump);
		context.waitTick();
		context.getInput().releaseKey(options -> options.keyJump);
	}

	private static void testElytraArmorFlight(final ClientGameTestContext context, final TestServerContext server) {
		Vec3 start = server.computeOnServer(s -> player(s).position());
		server.runCommand("item replace entity @a armor.chest with qualityofmine:elytra_armor");
		server.runCommand("tp @a ~ ~60 ~ 0 10");
		context.waitTicks(3);
		pressJump(context);
		context.waitTicks(5);
		check(context.computeOnClient(m -> m.player.isFallFlying()), "Mit der Elytra-Rüstung gleitet der Spieler");
		check(server.computeOnServer(s -> player(s).isFallFlying()), "Der Server sieht den Gleitflug");

		pressJump(context);
		context.waitTicks(5);
		check(rocketsNearPlayer(server) == 0, "Ohne Ladung gibt die Sprungtaste keinen Schub");
		check(context.computeOnClient(m -> m.player.isFallFlying()), "Ohne Ladung gleitet der Spieler einfach weiter");

		server.runOnServer(s -> ElytraArmors.setCharge(player(s).getItemBySlot(EquipmentSlot.CHEST), RocketCharge.MAX_BOOSTS));
		context.waitTicks(2);
		double before = horizontalSpeed(context);
		pressJump(context);
		context.waitTicks(3);
		check(rocketsNearPlayer(server) == 1, "Mit Ladung gibt die Sprungtaste einen Schub wie eine Rakete");
		check(server.computeOnServer(s -> ElytraArmors.chargeOf(player(s).getItemBySlot(EquipmentSlot.CHEST))) == RocketCharge.MAX_BOOSTS - 1,
			"Ein Schub kostet eine Ladung");
		context.waitTicks(7);
		double after = horizontalSpeed(context);
		LOGGER.info("[QoM-Test] Geschwindigkeit vor dem Schub {}, danach {}", before, after);
		check(after > before + 0.5, "Der Schub beschleunigt den Spieler");
		check(server.computeOnServer(s -> player(s).getHealth() == player(s).getMaxHealth()), "Der Schub verletzt den Spieler nicht");

		context.runOnClient(m -> m.options.setCameraType(CameraType.THIRD_PERSON_BACK));
		context.waitTicks(2);
		context.takeScreenshot("qom_elytra_armor_back");
		context.runOnClient(m -> m.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
		context.waitTicks(2);
		context.takeScreenshot("qom_elytra_armor_front");
		context.runOnClient(m -> m.options.setCameraType(CameraType.FIRST_PERSON));

		server.runCommand("item replace entity @a armor.chest with minecraft:air");
		server.runCommand(String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f", start.x, start.y, start.z));
		context.waitFor(m -> m.player.onGround());
		server.runOnServer(s -> player(s).setHealth(player(s).getMaxHealth()));
	}

	/** Bildschirmfoto der Ladebalken und Symbole: geladen und beschädigt, Netherit mit wenig Ladung, ungeladen. */
	private static void testElytraArmorInventory(final ClientGameTestContext context, final TestServerContext server) {
		server.runCommand("item replace entity @a hotbar.0 with qualityofmine:elytra_armor[qualityofmine:rocket_charge=70,damage=300]");
		server.runCommand("item replace entity @a hotbar.1 with qualityofmine:netherite_elytra_armor[qualityofmine:rocket_charge=20]");
		server.runCommand("item replace entity @a hotbar.2 with qualityofmine:elytra_armor");
		server.runCommand("item replace entity @a armor.chest with qualityofmine:elytra_armor[qualityofmine:rocket_charge=96]");
		context.waitTicks(5);
		context.runOnClient(m -> m.options.setCameraType(CameraType.THIRD_PERSON_BACK));
		context.waitTicks(2);
		context.takeScreenshot("qom_elytra_armor_jetpack");
		context.runOnClient(m -> m.options.setCameraType(CameraType.FIRST_PERSON));
		context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
		context.waitTicks(5);
		context.takeScreenshot("qom_elytra_armor_inventory");
		context.setScreen(() -> null);
		for (String slot : List.of("hotbar.0", "hotbar.1", "hotbar.2", "armor.chest")) {
			server.runCommand("item replace entity @a " + slot + " with minecraft:air");
		}
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
