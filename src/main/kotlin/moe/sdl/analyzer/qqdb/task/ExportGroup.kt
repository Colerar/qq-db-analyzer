package moe.sdl.analyzer.qqdb.task

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import moe.sdl.analyzer.qqdb.MainCommand
import moe.sdl.analyzer.qqdb.db.GroupMessage
import moe.sdl.analyzer.qqdb.db.MessageChain
import moe.sdl.analyzer.qqdb.db.json
import mu.KotlinLogging
import okio.buffer
import okio.sink
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

private val logger = KotlinLogging.logger {}

class ExportGroup(
  private val command: MainCommand,
  private val options: MainCommand.ExportGroupOption,
) {
  private val table: GroupMessage = GroupMessage(options.groupId)

  suspend fun run(
    dispatcher: CoroutineDispatcher = Dispatchers.IO
  ) = withContext(dispatcher) {
    transaction {
      SchemaUtils.create(table)
    }

    val where =
      (table.time greaterEq options.selectRange.first.epochSeconds) and
        (table.time lessEq options.selectRange.second.epochSeconds) and
        (table.type eq 1024) // 1024: 普通消息

    val file = File(command.outputPath, "${options.groupId}-messages.txt").apply {
      if (exists()) {
        logger.info { "输出文件被占用, 将删除: ${this.normalize().absolutePath}" }
        delete()
      }
      parentFile?.mkdirs()
      createNewFile()
    }

    val sink = file.sink()

    transaction {
      val count = table.select(where).count()
      logger.info { "预计将输出 $count 条聊天记录" }
    }

    val flow =
      table.select(where).apply { logger.info { "预计将输出 ${count()} 条聊天记录" } }
        .asFlow().mapNotNull { it[table.content] }
        .flatMapMerge { chain ->
          json.decodeFromString<MessageChain>(chain).filter {
            it.type == 0
          }.mapNotNull { it.text }.asFlow()
        }

    sink.buffer().use { buffered ->
      newSuspendedTransaction {
        flow.flatMapMerge { strs ->
          strs.split('\n').filterNot { it.isNullOrBlank() }.asFlow()
        }.collect {
          buffered.writeUtf8("$it\n")
        }
      }
    }

    logger.info { "输出完毕! 存放于文件: ${file.normalize().absolutePath}" }
  }
}
