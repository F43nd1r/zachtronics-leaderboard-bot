package com.faendir.zachtronics.bot.utils

interface Tree<T> {
    val data: T
    val children: List<Tree<T>>

    fun parentOf(child: T): T? = if (children.any { it.data == child }) data else children.asSequence().map { parentOf(child) }.firstOrNull()

    fun walkTree() : Sequence<T> = listOf(data).asSequence() + children.asSequence().flatMap { it.walkTree() }
}

class Forest<T>(val trees : List<Tree<T>>) {


    fun walkTrees() : Sequence<T> = trees.asSequence().flatMap { it.walkTree() }

    fun parentOf(child: T): T? = trees.asSequence().map { parentOf(child) }.firstOrNull()
}

class MutableTree<T>(override var data: T, override val children: MutableList<Tree<T>> = mutableListOf()) : Tree<T> {}