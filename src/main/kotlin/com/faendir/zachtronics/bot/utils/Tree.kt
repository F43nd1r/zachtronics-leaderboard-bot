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
    // not a set because [DataNode] equals/hashCode is mutable
    protected val children: MutableList<DataNode<T>> = mutableListOf()

    fun add(child: T): DataNode<T> = DataNode(child).also { children.add(it) }

    fun addPath(path: List<T>) {
        if (path.isEmpty()) return
        val data = path.first()
        (children.find { it.data == data } ?: add(data)).addPath(path.drop(1))
    }

    fun removeAllChildren() {
        children.clear()
    }

    fun getChildren(): Set<DataNode<T>> = children.toSet()

    abstract fun getAllPaths(): Set<List<T>>
}

class DataNode<T>(val data: T) : Node<T>() {
    override fun equals(other: Any?): Boolean {
        val otherNode = other as? DataNode<*> ?: return false
        return data == otherNode.data && children.toSet() == otherNode.children.toSet()
    }

    override fun hashCode(): Int = data.hashCode() + 31 * children.toSet().hashCode()

    override fun getAllPaths(): Set<List<T>> =
        if (children.isEmpty()) setOf(listOf(data)) else children.flatMap { node -> node.getAllPaths().map { listOf(data) + it } }.toSet()
}

class TreeRoot<T> : Node<T>() {
    override fun getAllPaths(): Set<List<T>> = children.flatMapTo(mutableSetOf()) { it.getAllPaths() }
}