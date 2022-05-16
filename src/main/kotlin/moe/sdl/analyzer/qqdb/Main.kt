package moe.sdl.analyzer.qqdb

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.defaultByName
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.mordant.rendering.TextColors.yellow
import com.github.ajalt.mordant.rendering.TextStyles.bold
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import moe.sdl.analyzer.qqdb.config.VERSION
import moe.sdl.analyzer.qqdb.db.loadSQLite
import moe.sdl.analyzer.qqdb.i18n.ZhCnLanguage
import moe.sdl.analyzer.qqdb.task.ExportGroup
import moe.sdl.analyzer.qqdb.util.getWorkDir
import moe.sdl.analyzer.qqdb.util.parseDateRange
import moe.sdl.analyzer.qqdb.util.rangeOf
import mu.KotlinLogging
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun main(args: Array<String>): Unit = MainCommand().main(args)

private val logger = KotlinLogging.logger {}

class MainCommand : CliktCommand(
  name = "qq-db-analyzer",
  help = "版本: ${(yellow + bold)(VERSION)}",
  invokeWithoutSubcommand = true,
  printHelpOnEmptyArgs = true,
) {
  init {
    context {
      localization = ZhCnLanguage
    }
  }

  val databaseFile by option("--database-path", "-I", help = "SQLite 数据库路径")
    .file(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)
    .required()
    .check("需为 .db 数据库文件") { it.extension == "db" }

  val task: OptionGroup by option("--type", "-T", help = "执行的任务类型")
    .groupChoice(
      "export-group-msg" to ExportGroupOption()
    ).defaultByName("export-group-msg")

  class ExportGroupOption : OptionGroup("export-group-msg") {
    val groupId by option("--group-id", "-G", help = "用于任务的群号").long().required()
    val selectRange by option("--date-range", "-D").convert {
      parseDateRange(it) ?: throw UsageError("区间输入错误")
    }.defaultLazy {
      val now = Clock.System.now()
      logger.info { "未输入解析区间, 默认输出过去一天" }
      rangeOf(now - 1.toDuration(DurationUnit.DAYS), now)
    }
  }

  val outputPath by option("--output-path", "-O", help = "输出路径")
    .file(mustExist = true, canBeFile = true, canBeDir = true, mustBeReadable = true)
    .default(getWorkDir())

  override fun run() = runBlocking {
    loadSQLite(databaseFile)

    when (task) {
      is ExportGroupOption -> {
        ExportGroup(this@MainCommand, (task as ExportGroupOption)).run()
      }
    }
  }
}
