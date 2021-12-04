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
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

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

fun ClassName.withParserSuffix() = ClassName(packageName, "${simpleNames.joinToString("_")}Parser")

private val primitives = listOf(BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BOOLEAN, CHAR)

val TypeName.isPrimitive: Boolean
    get() = !isNullable && primitives.contains(this)

fun ClassName.asFqName() = FqName.fromSegments(listOf(packageName) + simpleNames)

fun ClassId.asClassName() = ClassName(packageFqName.asString(), relativeClassName.asString())

fun ClassName.mapToKotlin() = JavaToKotlinClassMap.mapJavaToKotlin(asFqName())?.asClassName() ?: this