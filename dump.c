#include <stdio.h>
#define KEY_LENGTH 16

// 需要 Libraries 里有 Hummer.framework 以动态链接
typedef struct sqlite3 sqlite3;
int sqlite3_open(const char *filename, sqlite3 **ppDb);
int sqlite3_exec(sqlite3 *, const char *sql, int (*callback)(void *, int, char **, char **), void *, char **errmsg);
int sqlite3_rekey(sqlite3 *db, const void *pKey, int nKey);
int sqlite3_key(sqlite3 *db, const void *pKey, int nKey);

int main() {
  sqlite3 *db;
  int ret;

  // 在这里填数据库路径, 建议对数据库副本操作, 以免造成不必要的损失
  ret = sqlite3_open("path_here", &db);
  printf("QQ SQLITE_OPEN %d\n", ret);

  // 16位QQ数据库密码, 动调获取方法可以参见:
  // https://www.52pojie.cn/thread-1335657-1-1.html
  ret = sqlite3_key(db, "password_here", KEY_LENGTH);
  printf("QQ SQLITE_KEY %d\n", ret);

  char *zErrMsg = 0;
  ret = sqlite3_exec(db, "SELECT name FROM sqlite_master WHERE type='table';", 0, 0, &zErrMsg);
  printf("QQ SQLITE_EXEC: %d; MSG: %s\n", ret, zErrMsg);

  ret = sqlite3_rekey(db, "", 0);
  printf("QQ SQLITE_REKEY: %d\n", ret);

  return 0;
}
