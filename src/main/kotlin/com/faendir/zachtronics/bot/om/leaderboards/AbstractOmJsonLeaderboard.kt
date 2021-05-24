package com.faendir.zachtronics.bot.om.leaderboards

import com.faendir.zachtronics.bot.main.git.GitRepository
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.model.UpdateResult
import com.faendir.zachtronics.bot.om.imgur.ImgurService
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.utils.plusIf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import reactor.core.publisher.Mono
import java.io.File
import javax.annotation.PostConstruct

abstract class AbstractOmJsonLeaderboard<J>(
    private val gitRepo: GitRepository, private val imgurService: ImgurService,
    private val directoryCategories: Map<String, List<OmCategory>>, private val serializer: KSerializer<J>
) :
    Leaderboard<OmCategory, OmPuzzle, OmRecord> {
    companion object {
        private const val scoreFileName = "scores.json"
    }


    override val supportedCategories: List<OmCategory>
        get() = directoryCategories.values.flatten()

    @PostConstruct
    fun onStartUp() {
        gitRepo.access {
            directoryCategories.forEach { (dirName, categories) ->
                val dir = File(repo, dirName)
                File(dir, scoreFileName).takeIf { it.exists() }?.let { file ->
                    updatePage(dir, categories, Json.decodeFromString(serializer, file.readText()))
                }
            }
            if (status().run { added.isNotEmpty() || changed.isNotEmpty() }) {
                commitAndPush("Update page formatting")
            }
        }.block()
    }

    override fun update(puzzle: OmPuzzle, record: OmRecord): Mono<UpdateResult> {
        return gitRepo.access {
            val betterExists = mutableMapOf<OmCategory, OmScore>()
            val success = mutableMapOf<OmCategory, OmScore?>()
            val rehostedLink by lazy { imgurService.tryRehost(record.link) }
            var paretoUpdate = false
            directoryCategories.forEach { (dirName, dirCategories) ->
                val dir = File(repo, dirName)
                val scoreFile = File(dir, scoreFileName)
                val records = getRecords(dirName)
                val categories = dirCategories.filter { it.supportsPuzzle(puzzle) && it.supportsScore(record.score) }
                var changed = false
                for (category in categories) {
                    val oldRecord = records.getRecord(puzzle, category)
                    if (oldRecord == null || category.scoreComparator.compare(record.score, oldRecord.score).let {
                            it < 0 || it == 0 && oldRecord.link != record.link
                        }) {
                        records.setRecord(puzzle, category, OmRecord(category.normalizeScore(record.score), rehostedLink))
                        changed = true
                        success[category] = oldRecord?.score?.also { it.displayAsSum = category.name.startsWith("S") }
                    } else {
                        betterExists[category] = oldRecord.score.also { it.displayAsSum = category.name.startsWith("S") }
                    }
                }
                val localParetoUpdate = paretoUpdate(puzzle, record, records)
                if (changed || localParetoUpdate) {
                    scoreFile.writeText(Json { prettyPrint = true }.encodeToString(serializer, records))
                    add(scoreFile)
                    updatePage(dir, dirCategories, records)
                }
                paretoUpdate = paretoUpdate || localParetoUpdate
            }
            if (status().run { added.isNotEmpty() || changed.isNotEmpty() }) {
                commitAndPush(record.author, puzzle, record.score, success.map { it.key.displayName }.plusIf(paretoUpdate, "PARETO"))
            }
            when {
                success.isNotEmpty() -> {
                    record.score.displayAsSum = success.keys.any { it.name.startsWith("S") }
                    UpdateResult.Success(success)
                }
                paretoUpdate -> UpdateResult.ParetoUpdate()
                betterExists.isNotEmpty() -> UpdateResult.BetterExists(betterExists)
                else -> UpdateResult.NotSupported()
            }
        }
    }

    override fun get(puzzle: OmPuzzle, category: OmCategory): Mono<OmRecord> {
        return gitRepo.access {
            getRecords(category)?.getRecord(puzzle, category)?.also { it.score.displayAsSum = category.name.startsWith("S") }
        }
    }

    private var cached: MutableMap<String, J> = mutableMapOf()
    private var hash: String? = null

    private fun GitRepository.AccessScope.getRecords(category: OmCategory): J? {
        val (dirName, _) = directoryCategories.asIterable().find { it.value.contains(category) } ?: return null
        return getRecords(dirName)
    }

    private fun GitRepository.AccessScope.getRecords(dirName: String): J {
        val currentHash = currentHash()
        return if (hash == currentHash && cached.containsKey(dirName)) {
            cached.getValue(dirName)
        } else {
            Json.decodeFromString(serializer, File(File(repo, dirName), scoreFileName).takeIf { it.exists() }?.readText() ?: "{}").also {
                if(hash != currentHash) {
                    cached.clear()
                    hash = currentHash
                }
                cached[dirName] = it
            }
        }
    }

    protected open fun paretoUpdate(puzzle: OmPuzzle, record: OmRecord, records: J): Boolean = false

    protected abstract fun J.getRecord(puzzle: OmPuzzle, category: OmCategory): OmRecord?

    protected abstract fun J.setRecord(puzzle: OmPuzzle, category: OmCategory, record: OmRecord)

    protected abstract fun GitRepository.AccessScope.updatePage(dir: File, categories: List<OmCategory>, records: J)
}