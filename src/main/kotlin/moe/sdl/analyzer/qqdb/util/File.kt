package moe.sdl.analyzer.qqdb.util

import java.io.File

fun getWorkDir() = File(System.getProperty("user.dir") ?: error("Failed to read 'user.dir'"))
