/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.platforminitiatives.model

import play.api.libs.json.{Format, JsError, JsObject, JsString, JsSuccess, JsValue}

case class Version(
  major   : Int,
  minor   : Int,
  patch   : Int,
  original: String = ""
) extends Ordered[Version]:

  override def compare(other: Version): Int =
    import Ordered._
    (major, minor, patch).compare((other.major, other.minor, other.patch))

object Version:

  given ordering: Ordering[Version] =
    new Ordering[Version]:
      def compare(x: Version, y: Version): Int =
        x.compare(y)

  def apply(s: String): Version =
    val regex3 = """(\d+)\.(\d+)\.(\d+)(.*)""".r
    val regex2 = """(\d+)\.(\d+)(.*)""".r
    val regex1 = """(\d+)(.*)""".r
    s match
      case regex3(maj, min, patch, _) => Version(Integer.parseInt(maj), Integer.parseInt(min), Integer.parseInt(patch), s)
      case regex2(maj, min, _)        => Version(Integer.parseInt(maj), Integer.parseInt(min), 0, s)
      case regex1(patch, _)           => Version(0, 0, Integer.parseInt(patch), s)
      case _                          => Version(0, 0, 0, s)

  val format: Format[Version] =
    new Format[Version]:
      override def reads(json: JsValue) =
        json match
          case JsString(s)  => JsSuccess(Version(s))
          case JsObject(m)  => m.get("original") match
                                case Some(JsString(s)) => JsSuccess(Version(s))
                                case _                 => JsError("Not a string")
          case _            => JsError("Not a string")

      override def writes(v: Version) =
        JsString(v.original)
