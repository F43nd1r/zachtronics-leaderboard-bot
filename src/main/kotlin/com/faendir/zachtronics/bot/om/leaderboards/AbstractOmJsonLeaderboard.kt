package com.faendir.zachtronics.bot.om.leaderboards

import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.om.imgur.ImgurService
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.utils.plusIf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
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
            }
    }

    override fun update(puzzle: OmPuzzle, record: OmRecord): UpdateResult = gitRepo.access {
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
                    success[category] = oldRecord?.score
                } else {
                    betterExists[category] = oldRecord.score
                }
            }
            val localParetoUpdate = paretoUpdate(puzzle, record, records)
            if (changed || localParetoUpdate) {
                scoreFile.parentFile.mkdirs()
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
                success.keys.filter { it.name.startsWith("S") }.forEach { record.score.displaySums.add(it.requiredParts) }
                UpdateResult.Success(success)
            }
            paretoUpdate -> UpdateResult.ParetoUpdate()
            betterExists.isNotEmpty() -> UpdateResult.BetterExists(betterExists)
            else -> UpdateResult.NotSupported()
        }
    }

    override fun get(puzzle: OmPuzzle, category: OmCategory): OmRecord? = gitRepo.access {
        getRecords(category)?.getRecord(puzzle, category)
    }

    override fun getAll(puzzle: OmPuzzle, categories: Collection<OmCategory>): Map<OmCategory, OmRecord> = gitRepo.access {
        categories.groupBy { category -> directoryCategories.asIterable().find { it.value.contains(category) }?.key }
            .flatMap { (dirName, categories) ->
                if (dirName != null) {
                    val records = getRecords(dirName)
                    categories.mapNotNull { category -> records.getRecord(puzzle, category)?.let { category to it } }
                } else {
                    emptyList()
                }
            }.toMap()
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
                if (hash != currentHash) {
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

    protected fun OmScore.updateTransient(category: OmCategory) = apply {
        modifier = category.modifier
        if (category.name.startsWith("S")) displaySums.add(category.requiredParts)
    }
}