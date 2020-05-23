// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

// WITH_REFLECT

import kotlin.reflect.full.*

class A(param: String) {
    val int: Int get() = 42
    val string: String = param
    var anyVar: Any? = null

    val List<IntRange>.extensionToList: Unit get() {}

    fun notAProperty() {}
}

fun box(): String {
    val props = A::class.memberProperties

    val names = props.map { it.name }.sorted()
    assert(names == listOf("anyVar", "int", "string")) { "Fail names: $props" }

    val stringProp = props.firstOrNull { it.name == "string" } ?: return "Fail, string not found: $props"
    return stringProp.get(A("OK")) as String
}
