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
import play.api.libs.json.{OFormat, __}

case class SlugDependencies(
    slugName    : String,
    depGroup    : String,
    depArtefact : String,
    depVersion  : String,
    teams       : Seq[String]
  )

object SlugDependencies {
    implicit val format: OFormat[SlugDependencies] = {
        ((__ \ "slugName"     ).format[String]
        ~ (__ \ "depGroup"    ).format[String]
        ~ (__ \ "depArtefact" ).format[String]
        ~ (__ \ "depVersion"  ).format[String]
        ~ (__ \ "teams"       ).format[Seq[String]]
        ) (SlugDependencies.apply, unlift(SlugDependencies.unapply))
    }
  }
