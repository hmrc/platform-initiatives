/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Format, __}

case class MetaArtefactDependency(
  repoName   : String,
  depGroup   : String,
  depArtefact: String,
  depVersion : String,
  teams      : Seq[String]
)

object MetaArtefactDependency:
  given formats: Format[MetaArtefactDependency] =
    ( (__ \ "repoName"   ).format[String]
    ~ (__ \ "depGroup"   ).format[String]
    ~ (__ \ "depArtefact").format[String]
    ~ (__ \ "depVersion" ).format[String]
    ~ (__ \ "teams"      ).format[Seq[String]]
    )(MetaArtefactDependency.apply, mad => Tuple.fromProductTyped(mad))
