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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Format, __}

case class SlugJdkVersion(
  slugName: String,
  version : Version,
  vendor  : String,
  kind    : String,
)

object SlugJdkVersion {
  implicit val format: Format[SlugJdkVersion] =
  ( (__ \ "name"   ).format[String]
  ~ (__ \ "version").format[Version](Version.format)
  ~ (__ \ "vendor" ).format[String]
  ~ (__ \ "kind"   ).format[String]
  ) (SlugJdkVersion.apply, sjv => Tuple.fromProductTyped(sjv))
}
