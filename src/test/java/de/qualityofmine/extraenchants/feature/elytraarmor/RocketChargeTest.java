package de.qualityofmine.extraenchants.feature.elytraarmor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RocketChargeTest {
	@Test
	void twoRocketsGiveNinetySixBoosts() {
		assertEquals(96, RocketCharge.MAX_BOOSTS);
		assertEquals(96, RocketCharge.load(0, 2));
	}

	@ParameterizedTest(name = "Ladung {0}, {1} angeboten: {2} passen")
	@CsvSource({
		"0, 1, 1",
		"0, 64, 2",
		"48, 64, 1",
		"49, 64, 0",
		"47, 64, 1",
		"96, 1, 0",
		"0, 0, 0"
	})
	void onlyWholeRocketsFitWithoutWastingCharge(final int charge, final int offered, final int expected) {
		assertEquals(expected, RocketCharge.rocketsThatFit(charge, offered));
	}

	@ParameterizedTest(name = "Ladung {0} ergibt {1} Pixel")
	@CsvSource({
		"96, 13",
		"48, 7",
		"1, 0",
		"0, 0"
	})
	void barShrinksWithCharge(final int charge, final int expectedWidth) {
		assertEquals(expectedWidth, RocketCharge.barWidth(charge, 13));
	}

	@Test
	void combinedArmorKeepsTheWearOfBothParts() {
		assertEquals(150, ElytraArmorAnvil.combinedDamage(100, 50));
	}

	@Test
	void earlierAnvilWorkMakesCombiningMoreExpensive() {
		assertEquals(ElytraArmorAnvil.BASE_COST, ElytraArmorAnvil.cost(0, 0));
		assertEquals(ElytraArmorAnvil.BASE_COST + 3 + 7, ElytraArmorAnvil.cost(3, 7));
	}
}
