/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.utils

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Metric

data class MetricsTreeItem(val metric: Metric, val category: Category?)

private val metricsTreeCache = mutableMapOf<Set<Category>, TreeRoot<MetricsTreeItem>>()

private fun Collection<Category>.toMetricsTree(): TreeRoot<MetricsTreeItem> = metricsTreeCache.getOrPut(toSet()) {
    TreeRoot<MetricsTreeItem>().also { tree ->
        for (category in this) {
            val last = category.metrics.last()
            tree.addPath(category.metrics.dropLast(1).map { MetricsTreeItem(it, null) } + MetricsTreeItem(last, category))
        }
    }
}

private fun Node<MetricsTreeItem>.collapseFullyPresentNodes(reference: Node<MetricsTreeItem>) {
    for (referenceChild in reference.getChildren()) {
        val child = getChildren().find { it.data == referenceChild.data } ?: continue
        if (child.data.metric.collapsible && child == referenceChild) {
            child.removeAllChildren()
        } else {
            child.collapseFullyPresentNodes(referenceChild)
        }
    }
}

/**
 * Returns category names separated by commas, with categories compacted if they're fully present.
 *
 * @receiver all actual categories
 * @param reference all possible categories
 */
fun Collection<Category>.smartFormat(reference: Collection<Category>): String {
    val metricsTree = toMetricsTree()
    metricsTree.collapseFullyPresentNodes(reference.toMetricsTree())
    val shortenedCategories = metricsTree.getAllPaths()
        .map { list ->
            val metrics = list.map { it.metric }
            val category = list.last().category
            category?.displayName ?: metrics.joinToString("") { it.displayName }
        }
    return shortenedCategories.joinToString(", ")
}

