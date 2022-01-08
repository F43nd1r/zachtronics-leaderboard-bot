/*
 * Copyright (c) 2021
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

package com.faendir.zachtronics.bot.discord

import discord4j.rest.util.Color

object Colors {
    @JvmField val SUCCESS = Color.of(0x388e3c)
    @JvmField val FAILURE = Color.of(0xd32f2f)
    @JvmField val UNCHANGED = Color.of(0xfbc02d)
    @JvmField val READ = Color.of(0x1976d2)
}