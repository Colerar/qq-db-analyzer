package moe.sdl.analyzer.qqdb.db

import org.jetbrains.exposed.sql.Table


@OptIn(ExperimentalUnsignedTypes::class)
class GroupMessage private constructor(name: String) : Table(name) {
  constructor(id: Long) : this("tb_TroopMsg_$id")

  val messageId = long("msgId")
  val userId = long("uin")
  val groupId = long("cluster_id").nullable()
  val sessionType = uinteger("session_type").default(0u).nullable()

  val time = long("time").nullable()
  val type = integer("type").nullable()
  val flag = integer("flag").default(0).nullable()
  val read = integer("read").nullable()
  val sendState = integer("sendState").default(0).nullable()

  val content = varchar("content", Int.MAX_VALUE).nullable()

  val random = integer("msgRandom").nullable()
  val sequence = integer("msgSeq").nullable()
  val msgRelatedFlag = uinteger("msgRelatedFlag").default(0u).nullable()
  val bubbleId = integer("bubbleid").nullable()

  val nickName = varchar("nickName", 512).nullable()

  val fontInfo = blob("fontInfo").nullable()
  val visible = integer("visiable").default(1).nullable()
  val errorCode = integer("errorCode").nullable()
  val picUrl = varchar("picUrl", 1024).nullable()
  val appShareID = uinteger("appShareID").default(0u).nullable()
  val moreFlag = integer("moreflag").nullable()
  val actionUrl = varchar("actionUrl", 1024).nullable()

  val roamMessage = uinteger("roamMsg").default(0u).nullable()
  val roamRandom = uinteger("roamRandom").default(0u).nullable()
  val roamTime = uinteger("roamTime").nullable()
  val conseqMsg = uinteger("conseqMsg").default(0u).nullable()
  val fileMsgType = integer("fileMsgType").default(0).nullable()
  val onlineFileId = uinteger("onlineFileId").nullable()

  val placeholderMsg = integer("placeholderMsg").default(0).nullable()
  val strShareExtra = varchar("strShareExtra", 1024)
  val richSource = integer("richSource").default(0).nullable()
  val exInfo = varchar("exInfo", 1024).nullable()
  val exData = blob("exData").nullable()
  val searchContent = varchar("searchContent", 1024).nullable()

  override val primaryKey: PrimaryKey = PrimaryKey(messageId)
}
