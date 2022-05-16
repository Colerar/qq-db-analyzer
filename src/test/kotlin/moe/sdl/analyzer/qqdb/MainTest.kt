package moe.sdl.analyzer.qqdb

import moe.sdl.analyzer.qqdb.config.TEST_DB_PATH
import moe.sdl.analyzer.qqdb.config.TEST_GROUP
import org.junit.jupiter.api.Test

class MainTest {
  @Test
  fun testGroup() {
    MainCommand().parse(listOf("-I", TEST_DB_PATH ?: error("No db path provided"), "-G", "$TEST_GROUP", "-D", "1mon~"))
  }
}
