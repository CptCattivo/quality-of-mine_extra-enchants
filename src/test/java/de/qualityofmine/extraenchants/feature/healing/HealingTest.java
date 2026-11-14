package de.qualityofmine.extraenchants.feature.healing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HealingTest {
	@ParameterizedTest
	@CsvSource({"0, 0", "1, 1200", "2, 800", "3, 400"})
	void intervalShrinksWithLevel(final int level, final int expectedTicks) {
		assertEquals(expectedTicks, Healing.intervalTicks(level));
	}

	@ParameterizedTest
	@CsvSource({"1, 1200", "2, 800", "3, 400"})
	void healsExactlyOncePerInterval(final int level, final int interval) {
		int heals = 0;
		for (int tick = 1; tick <= interval * 3; tick++) {
			if (Healing.healsAt(level, tick)) {
				heals++;
			}
		}
		assertEquals(3, heals);
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 400, 1200, 2400})
	void noHealingWithoutEnchantment(final int tick) {
		assertFalse(Healing.healsAt(0, tick));
	}

	@Test
	void noHealingRightAfterSpawn() {
		assertFalse(Healing.healsAt(3, 0));
		assertTrue(Healing.healsAt(3, 400));
	}

	@Test
	void healsHalfAHeart() {
		assertEquals(1.0F, Healing.HEAL_AMOUNT);
	}
}
