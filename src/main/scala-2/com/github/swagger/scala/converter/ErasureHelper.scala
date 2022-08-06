package com.github.swagger.scala.converter

import scala.reflect.runtime.universe.TermName

object ErasureHelper {

  def erasedOptionalPrimitives(cls: Class[_]): Map[String, Class[_]] = {
    import scala.reflect.runtime.universe
    val mirror = universe.runtimeMirror(cls.getClassLoader)

    val moduleSymbol = mirror.moduleSymbol(Class.forName(cls.getName))
    val ConstructorName = "apply"
    val companion: universe.Symbol = moduleSymbol.typeSignature.member(TermName(ConstructorName))
    val properties = if (companion.fullName.endsWith(ConstructorName)) {
      companion.asMethod.paramLists.flatten
    } else {
      val sym = mirror.staticClass(cls.getName)
      sym.selfType.members
        .filterNot(_.isMethod)
        .filterNot(_.isClass)
    }

    properties.flatMap { prop: universe.Symbol =>
      val maybeClass: Option[Class[_]] = prop.typeSignature.typeArgs.headOption.flatMap { signature =>
        if (signature.typeSymbol.isClass) {
          Option(mirror.runtimeClass(signature.typeSymbol.asClass))
        } else None
      }
      maybeClass.map(prop.name.toString.trim -> _)
    }.toMap
  }

}
