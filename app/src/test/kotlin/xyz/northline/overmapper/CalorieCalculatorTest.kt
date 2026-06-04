package xyz.northline.overmapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import xyz.northline.overmapper.domain.CalorieCalculator

class CalorieCalculatorTest {

    @Test
    fun `returns zero for zero duration`() {
        val result = CalorieCalculator.calculate(
            weightKg = 75f, durationMs = 0L, distanceM = 0f, elevationGainM = 0f
        )
        assertThat(result).isEqualTo(0f)
    }

    @Test
    fun `flat 5km walk at moderate pace for 75kg person`() {
        val result = CalorieCalculator.calculate(
            weightKg = 75f,
            durationMs = 60 * 60 * 1000L,
            distanceM = 5000f,
            elevationGainM = 0f
        )
        assertThat(result).isGreaterThan(300f)
        assertThat(result).isLessThan(500f)
    }

    @Test
    fun `steep climb burns more than flat walk at same distance and time`() {
        val flat = CalorieCalculator.calculate(75f, 3_600_000L, 5000f, 0f)
        val steep = CalorieCalculator.calculate(75f, 3_600_000L, 5000f, 400f)
        assertThat(steep).isGreaterThan(flat)
    }

    @Test
    fun `heavier person burns more calories`() {
        val light = CalorieCalculator.calculate(60f, 3_600_000L, 5000f, 100f)
        val heavy = CalorieCalculator.calculate(100f, 3_600_000L, 5000f, 100f)
        assertThat(heavy).isGreaterThan(light)
    }
}
