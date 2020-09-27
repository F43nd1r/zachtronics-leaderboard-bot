package com.faendir.zachtronics.bot.leaderboards.om

import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.model.om.OmCategory
import com.faendir.zachtronics.bot.model.om.OmPuzzle
import com.faendir.zachtronics.bot.model.om.OmRecord
import com.faendir.zachtronics.bot.model.om.OmScore

interface OmLeaderboard : Leaderboard<OmCategory, OmScore, OmPuzzle, OmRecord>