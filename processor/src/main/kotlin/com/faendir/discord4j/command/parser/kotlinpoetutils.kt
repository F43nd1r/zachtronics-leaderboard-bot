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

package com.faendir.discord4j.command.parser

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

fun KSTypeReference.asTypeName() = resolve().asTypeName()

fun KSType.asTypeName(): TypeName {
    var name: TypeName = asClassName()
    if (arguments.isNotEmpty()) {
        name = (name as ClassName).parameterizedBy(arguments.map { it.type!!.asTypeName() })
    }
    return if (isMarkedNullable) name.copy(true) else name
}

fun KSType.asClassName(): ClassName {
    return ClassName(declaration.normalizedPackageName,
        *generateSequence(declaration.parentDeclaration) { it.parentDeclaration }.map { it.simpleName.asString() }.toList().toTypedArray(),
        declaration.simpleName.asString()
    )
}

fun FileSpec.writeTo(source: KSFile, codeGenerator: CodeGenerator) {
    codeGenerator.createNewFile(Dependencies(false, source), packageName, name).writer().use { writeTo(it) }
}

@Suppress("UNCHECKED_CAST")
val <T : TypeName> T.nonnull: T
    get() = copy(nullable = false) as T

fun TypeName.toRawType(): ClassName = when (this) {
    is ParameterizedTypeName -> this.rawType
    is ClassName -> this
    else -> throw IllegalArgumentException()
}

fun ClassName.withParserSuffix() = ClassName(packageName, "${simpleNames.joinToString("_")}Parser")

fun TypeName.withParserSuffix() = toRawType().withParserSuffix()

fun TypeName.asLambdaReceiver() = LambdaTypeName.get(receiver = this, returnType = Unit::class.asClassName())

private val primitives = listOf(BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BOOLEAN, CHAR)

val TypeName.isPrimitive: Boolean
    get() = !isNullable && primitives.contains(this)