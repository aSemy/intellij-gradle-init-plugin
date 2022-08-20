package dev.adamko.intellij.gradle_init

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


class DynamicCastDelegate<out T : Any>(
  private val cls: KClass<T>,
  delegateProvider: () -> Any,
) : InvocationHandler {

  private val delegate = delegateProvider()

  private val delegateName = delegate.javaClass.name
  private val delegateMethods = delegate.javaClass.methods

  private val proxy: T by lazy {
//    try {
    val proxy = Proxy.newProxyInstance(
      delegate.javaClass.classLoader,
      arrayOf(cls.java),
      this,
    )
    @Suppress("UNCHECKED_CAST")
    proxy as T
//    } catch (ex: ClassCastException) {
//      null
//    }
  }

  override fun invoke(
    proxy: Any,
    method: Method,
    args: Array<out Any?>?
  ): Any? {
    for (delegateMethod in delegateMethods) {
      if (method matches delegateMethod) {
        return if (args == null)
          delegateMethod.invoke(delegate)
        else
          delegateMethod.invoke(delegate, *args)
      }
    }
    throw UnsupportedOperationException("$delegateName : $method args:[${args?.joinToString()}]")
  }

  /** Delegated value provider */
  operator fun getValue(thisRef: Any?, property: KProperty<*>): T = proxy


  companion object {
    private infix fun Method.matches(other: Method): Boolean =
      this.name == other.name && this.parameterTypes.contentEquals(other.parameterTypes)


    inline fun <reified T : Any> dynamicCast(noinline delegate: () -> Any): DynamicCastDelegate<T> {
      return DynamicCastDelegate(T::class, delegate)
    }
  }
}

//
//class A {
//  val name: String = "Team A"
//  fun shout() = println("go team A!")
//  fun echo(input: String) = input.repeat(5)
//}
//
//class B {
//  val name: String = "Team B"
//  fun shout() = println("go team B!")
//  fun echo(call: String) = call.repeat(2)
//}
//
//interface Shoutable {
//  val name: String
//  fun shout()
//  fun echo(call: String): String
//}
//
//
//fun main(args: Array<String>) {
//  val a = A()
//  val b = B()
//
//  val sa by dynamicCast<Shoutable> { a }
//  val sb: Shoutable? by dynamicCast { b }
//
//  sa?.shout()
//  sb?.shout()
//  println(sa?.echo("hello..."))
//  println(sb?.echo("hello..."))
//  println(sa?.name)
//  println(sb?.name)
//}
////
