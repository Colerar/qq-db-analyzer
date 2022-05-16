package moe.sdl.analyzer.qqdb

import kotlinx.datetime.Instant
import moe.sdl.analyzer.qqdb.util.parseDateRange
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class ParseRangeTest {
  @Test
  fun test() {
    parseDateRange("wocaobushiba").print()

    parseDateRange("11-1~12-1").print()
    parseDateRange("12-1~11-1").print()

    parseDateRange("1mon~11-1").print()
    parseDateRange("1mon~12-1").print()

    parseDateRange("1mon~").print()
    parseDateRange("11-1~1mon").print()
  }

  private fun Pair<Instant, Instant>?.print() {
    if (this == null) {
      println("Parsed: null")
    } else {
      println("Parsed: $first to $second")
    }
  }
}
