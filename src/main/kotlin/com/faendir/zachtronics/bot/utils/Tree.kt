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

package com.faendir.zachtronics.bot.utils

abstract class Node<T> {
    protected val children: MutableList<DataNode<T>> = mutableListOf()

    fun addPath(path: List<T>) {
        if (path.isEmpty()) return
        val data = path.first()
        (children.find { it.data == data } ?: DataNode(data).also { children.add(it) }).addPath(path.drop(1))
    }

    fun collapseFullyPresentNodes(reference: Node<T>) {
        for (referenceChild in reference.children) {
            val child = children.find { it.data == referenceChild.data } ?: continue
            if (child == referenceChild) {
                child.children.clear()
            } else {
                child.collapseFullyPresentNodes(referenceChild)
            }
        }
    }
}

class DataNode<T>(val data: T) : Node<T>() {
    override fun equals(other: Any?): Boolean {
        val otherNode = other as? DataNode<*> ?: return false
        return data == otherNode.data && children.toSet() == otherNode.children.toSet()
    }

    override fun hashCode(): Int = data.hashCode() + 31 * children.toSet().hashCode()

    fun getAllPaths(): Set<List<T>> =
        if (children.isEmpty()) setOf(listOf(data)) else children.flatMap { node -> node.getAllPaths().map { listOf(data) + it } }.toSet()
}

class TreeRoot<T> : Node<T>() {
    fun getAllPaths() = children.flatMap { it.getAllPaths() }
}