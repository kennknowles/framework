/*
 * Copyright 2009-2011 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liftweb
package json

import org.specs.Specification

object ExtractionBugs extends Specification("Extraction bugs Specification") {
  implicit val formats = DefaultFormats
  
  "ClassCastException (BigInt) regression 2 must pass" in {
    val opt = OptionOfInt(Some(39))    
    Extraction.decompose(opt).extract[OptionOfInt].opt.get mustEqual 39
  }

  "Extraction should not fail when Maps values are Lists" in {
    val m = PMap(Map("a" -> List("b"), "c" -> List("d")))
    Extraction.decompose(m).extract[PMap] mustEqual m
  }

  "Extraction should always choose constructor with the most arguments if more than one constructor exists" in {
    val args = Meta.Reflection.primaryConstructorArgs(classOf[ManyConstructors])
    args.size mustEqual 4
  }

  "Extraction should handle AnyRef" in {
    implicit val formats = DefaultFormats.withHints(FullTypeHints(classOf[ExtractWithAnyRef] :: Nil))
    val json = JObject(JField("jsonClass", JString(classOf[ExtractWithAnyRef].getName)) :: Nil)
    val extracted = Extraction.extract[AnyRef](json)
    extracted mustEqual ExtractWithAnyRef()
  }

  "Extraction should work with unicode encoded field names (issue 1075)" in {
    parse("""{"foo.bar,baz":"x"}""").extract[UnicodeFieldNames] mustEqual UnicodeFieldNames("x")
  }

  "Extraction should not fail if case class has a companion object" in {
    parse("""{"nums":[10]}""").extract[HasCompanion] mustEqual HasCompanion(List(10))
  }

  case class OptionOfInt(opt: Option[Int])

  case class PMap(m: Map[String, List[String]])

  case class ManyConstructors(id: Long, name: String, lastName: String, email: String) {
    def this() = this(0, "John", "Doe", "")
    def this(name: String) = this(0, name, "Doe", "")
    def this(name: String, email: String) = this(0, name, "Doe", email)
  }

  case class ExtractWithAnyRef()

  case class UnicodeFieldNames(`foo.bar,baz`: String)

  object HasCompanion {
    def hello = "hello"
  }
  case class HasCompanion(nums: List[Int])
}
