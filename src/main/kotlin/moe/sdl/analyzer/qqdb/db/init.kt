package moe.sdl.analyzer.qqdb.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.io.File
import java.sql.Connection

fun loadSQLite(file: File): Database {
  val path = file.normalize().absolutePath
  val source = HikariConfig().apply {
    jdbcUrl = "jdbc:sqlite:$path"
    driverClassName = "org.sqlite.JDBC"
    maximumPoolSize = 1 // SQLite 只支持至多一个连接
  }.let { HikariDataSource(it) }

  TransactionManager.manager.defaultIsolationLevel =
    Connection.TRANSACTION_READ_UNCOMMITTED // QQ 数据库分析是只读的，使用最低级别提高性能

  return Database.connect(source)
}
