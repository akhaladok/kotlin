/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlinx.serialization.compiler.resolve

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.psi.KtDeclarationWithInitializer
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.ValueArgument
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.isInlineClassType
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlinx.serialization.compiler.backend.common.analyzeSpecialSerializers

class SerializableProperty(
    val descriptor: PropertyDescriptor,
    val isConstructorParameterWithDefault: Boolean,
    hasBackingField: Boolean
) {
    val name = descriptor.annotations.serialNameValue ?: descriptor.name.asString()
    val type = descriptor.type
    val genericIndex = type.genericIndex
    val module = descriptor.module
    val serializableWith = descriptor.serializableWith ?: analyzeSpecialSerializers(module, descriptor.annotations)?.defaultType
    val optional = !descriptor.annotations.serialRequired && descriptor.declaresDefaultValue
    val transient = descriptor.annotations.serialTransient || !hasBackingField
    val annotationsWithArguments: List<Triple<ClassDescriptor, List<ValueArgument>, List<ValueParameterDescriptor>>> =
        descriptor.annotationsWithArguments()

    val canBeInlined: Boolean = type.isInlineClassType() && !type.isMarkedNullable

    override fun toString(): String {
        return "Property $name : $type in ${descriptor.containingDeclaration.fqNameOrNull() ?: "UNKNOWN"}"
    }
}

val PropertyDescriptor.declaresDefaultValue: Boolean
    get() = when (val declaration = this.source.getPsi()) {
        is KtDeclarationWithInitializer -> declaration.initializer != null
        is KtParameter -> declaration.defaultValue != null
        else -> false
    }