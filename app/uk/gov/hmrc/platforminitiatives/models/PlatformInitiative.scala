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

package uk.gov.hmrc.platforminitiatives.models

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Json, OFormat, OWrites, Reads, Writes, __}

case class Progress(
  current : Int,
  target  : Int
)

object Progress {
  implicit val format: OFormat[Progress] = {
    ((__ \ "current").format[Int]
      ~ (__ \ "target").format[Int]
      )(Progress.apply,unlift(Progress.unapply))
  }
}


case class PlatformInitiative(
  initiativeName            : String,
  initiativeDescription     : String,
  progress                  : Progress,
  completedLegend           : String,
  inProgressLegend          : String,
  experimental              : Boolean
)

object PlatformInitiative {

  def ignore[A]: OWrites[A] = OWrites[A](_ => Json.obj())
  implicit val writes: OWrites[PlatformInitiative] = {
    ((__ \ "initiativeName"           ).write[String]
      ~ (__ \ "initiativeDescription" ).write[String]
      ~ (__ \ "progress"              ).write[Progress]
      ~ (__ \ "completedLegend"       ).write[String]
      ~ (__ \ "inProgressLegend"      ).write[String]
      ~ ignore[Boolean]
      ) (unlift(PlatformInitiative.unapply))
  }
}