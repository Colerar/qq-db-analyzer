package moe.sdl.analyzer.qqdb.util

import kotlinx.datetime.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

// 日期解析
//
// 基本解析: 2022-1-1~2022-10-1
// 不写年则默认今年: 1-1~10-1
// 只有起始时间限制: 3-12~
// 只有截止时间限制: ~2021-8-8
// 可以使用相对时间 2021-1-4~30d
//               2021-12-12~2m
//               2021-9-24~1y
//
// 波浪号的结束时间可以省略，默认今天
// 1m~ 即为过去一个月
//
// 表达式中的空格一律移除

private val dateComplete by lazy {
  Regex("""^(\d{1,4})-(\d{1,2})-(\d{1,2})$""")
}

private val dateMonthDay by lazy {
  Regex("""^(\d{1,2})-(\d{1,2})$""")
}

private val dateRangeCheck by lazy {
  Regex("""^[-a-zA-Z0-9]*~[-a-zA-Z0-9]*$""")
}

fun String.removeBlank() =
  filterNot { it.isWhitespace() }

/**
 * @param expression 区间表达式
 * @param timeZone 表达式对应的时区, 默认取系统时区
 * @return 起始时间和终止时间, 解析失败时为 null
 */
fun parseDateRange(
  expression: String,
  timeZone: TimeZone = TimeZone.currentSystemDefault()
): Pair<Instant, Instant>? {
  val str = expression.removeBlank()
  if (!dateRangeCheck.matches(str)) return null

  val parts = str.split('~')
  val a = parts.getOrNull(0)
  val b = parts.getOrNull(1)

  if (a == null && b == null) return null

  val aDate = a?.let { parseDate(it, timeZone) }
  val aDuration = a?.let { parseDuration(it) }

  if (aDate == null && aDuration == null) return null

  val bDate = b?.let { parseDate(it, timeZone) }
  val bDuration = b?.let { parseDuration(it) }

  // 有效情况共有四种:
  // 日期~日期
  // 日期~相对
  // 相对~日期
  // 相对~    结束时间默认今天

  return when {
    aDate != null && aDuration == null -> {
      when {
        bDate != null -> { // date~date
          rangeOf(aDate, bDate)
        }
        bDuration != null -> { // date~duration
          val bInstant = aDate + bDuration
          rangeOf(aDate, bInstant)
        }
        else -> null
      }
    }
    aDate == null && aDuration != null -> {
      when {
        b.isNullOrBlank() -> { // duration~    , bdate defaults to now
          val now = Clock.System.now()
          rangeOf(now - aDuration, now)
        }
        bDate != null -> { // duration~date
          rangeOf(bDate - aDuration, bDate)
        }
        else -> null
      }
    }
    else -> null
  }
}

/**
 * @return 返回验证器,
 * @see parseDateRange
 */
fun parseDateRangeToChecker(
  expression: String,
  timeZone: TimeZone = TimeZone.currentSystemDefault()
): ((Instant) -> Boolean)? = parseDateRange(expression, timeZone)?.toRangeChecker()

fun Pair<Instant, Instant>.toRangeChecker(): (Instant) -> Boolean =
  { instant: Instant -> this.first <= instant && this.second <= instant }

fun rangeOf(instant: Instant, other: Instant) =
  minOf(instant, other) to maxOf(instant, other)

fun parseDate(dateStr: String, timeZone: TimeZone): Instant? {
  val str = dateStr.removeBlank()
  val localDateTime = parseCompleteDate(str) ?: parseMonthDay(str, timeZone)
  return localDateTime?.toInstant(timeZone)
}

private fun parseCompleteDate(string: String): LocalDateTime? {
  val values = dateComplete.find(string)?.groupValues?.mapNotNull {
    it.toIntOrNull()
  }
  return if (values?.size == 3) {
    val (year, month, day) = values
    LocalDateTime(year, month, day, 0, 0, 0, 0)
  } else null
}

private fun parseMonthDay(string: String, timeZone: TimeZone): LocalDateTime? {
  val values = dateMonthDay.find(string)?.groupValues?.mapNotNull {
    it.toIntOrNull()
  }
  return if (values?.size == 2) {
    val (month, day) = values
    val thisYear = Clock.System.now().toLocalDateTime(timeZone).year
    LocalDateTime(thisYear, month, day, 0, 0, 0, 0)
  } else null
}


private val durationRegex by lazy {
  /* ktlint-disable max-line-length */
  Regex("""((\d{1,2})[yY年])?((\d{1,2})(月|mon|Mon))?((\d{1,2})[dD天])?((\d{1,2})(小时|时|h|H))?((\d{1,2})(分钟|分|m|M)(?!on))?((\d{1,7})(秒钟|秒|s|S))?""")
  /* ktlint-enable max-line-length */
}

private val yearRegex by lazy { Regex("""(\d{1,2})[yY年]""") }
private val monRegex by lazy { Regex("""(\d{1,2})(月|mon|Mon)""") }
private val dayRegex by lazy { Regex("""(\d{1,2})[dD天]""") }
private val hourRegex by lazy { Regex("""(\d{1,2})(小时|时|h|H)""") }
private val minRegex by lazy { Regex("""(\d{1,2})(分钟|分|m|M)(?!on)""") }
private val secRegex by lazy { Regex("""(\d{1,7})(秒钟|秒|s|S)""") }
private val numberRegex by lazy { Regex("""^\d+$""") }

fun parseDuration(expr: String): Duration? {
  if (numberRegex.matches(expr)) {
    return expr.toLongOrNull()?.toDuration(DurationUnit.SECONDS)
  }

  if (durationRegex.matches(expr)) {
    var totalTime: Long = 0
    totalTime += yearRegex.find(expr)?.groups?.get(1)?.value?.toLong()?.times(365 * 24 * 60 * 60) ?: 0L
    totalTime += monRegex.find(expr)?.groups?.get(1)?.value?.toLong()?.times(30 * 24 * 60 * 60) ?: 0L
    totalTime += dayRegex.find(expr)?.groups?.get(1)?.value?.toLong()?.times(24 * 60 * 60) ?: 0L
    totalTime += hourRegex.find(expr)?.groups?.get(1)?.value?.toLong()?.times(60 * 60) ?: 0L
    totalTime += minRegex.find(expr)?.groups?.get(1)?.value?.toLong()?.times(60) ?: 0L
    totalTime += secRegex.find(expr)?.groups?.get(1)?.value?.toLong() ?: 0L

    return totalTime.toDuration(DurationUnit.SECONDS)
  }

  return null
}
