/*
 * Copyright (c) 2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.testutils

import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.StoredConfig
import org.eclipse.jgit.storage.file.FileBasedConfig
import org.eclipse.jgit.util.FS
import org.eclipse.jgit.util.SystemReader
import org.eclipse.jgit.util.time.MonotonicClock
import java.nio.charset.Charset
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * A [SystemReader] that does not read any external config files.
 * This is necessary because jgit does not support commit signing but still tries to read the gpg config when present.
 */
class JGitNoExternalConfigReader(private val proxy: SystemReader) : SystemReader() {
    override fun getenv(key: String?): String? = proxy.getenv(key)
    override fun getProperty(key: String?): String? = proxy.getProperty(key)
    override fun getHostname(): String = proxy.hostname
    override fun getCurrentTime(): Long = proxy.currentTime
    override fun openUserConfig(parent: Config?, fs: FS?): FileBasedConfig = openJGitConfig(parent, fs)
    override fun openSystemConfig(parent: Config?, fs: FS?): FileBasedConfig = openJGitConfig(parent, fs)
    override fun openJGitConfig(parent: Config?, fs: FS?): FileBasedConfig = proxy.openJGitConfig(parent, fs)
    override fun getUserConfig(): StoredConfig = proxy.jGitConfig
    override fun getJGitConfig(): StoredConfig = proxy.jGitConfig
    override fun getSystemConfig(): StoredConfig = proxy.jGitConfig
    override fun getClock(): MonotonicClock = proxy.clock
    override fun getTimezone(`when`: Long): Int = proxy.getTimezone(`when`)
    override fun getTimeZone(): TimeZone = proxy.timeZone
    override fun getLocale(): Locale = proxy.locale
    override fun getDefaultCharset(): Charset = proxy.defaultCharset
    override fun getSimpleDateFormat(pattern: String?): SimpleDateFormat = proxy.getSimpleDateFormat(pattern)
    override fun getSimpleDateFormat(pattern: String?, locale: Locale?): SimpleDateFormat = proxy.getSimpleDateFormat(pattern, locale)
    override fun getDateTimeInstance(dateStyle: Int, timeStyle: Int): DateFormat = proxy.getDateTimeInstance(dateStyle, timeStyle)
    override fun isWindows(): Boolean = proxy.isWindows
    override fun isMacOS(): Boolean = proxy.isMacOS
    override fun isLinux(): Boolean = proxy.isLinux
    override fun isPerformanceTraceEnabled(): Boolean = proxy.isPerformanceTraceEnabled
    override fun checkPath(path: String?) = proxy.checkPath(path)
    override fun checkPath(path: ByteArray?) = proxy.checkPath(path)

    companion object {
        fun install() {
            setInstance(JGitNoExternalConfigReader(getInstance()))
        }
    }
}