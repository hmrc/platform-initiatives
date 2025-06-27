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

package uk.gov.hmrc.platforminitiatives.connector

import play.api.libs.json.{Reads, __}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.platforminitiatives.model.*
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ServiceConfigsConnector @Inject() (
  httpClientV2    : HttpClientV2,
  servicesConfig  : ServicesConfig,
)(using
  ec              : ExecutionContext
):

  import ServiceConfigsConnector.*

  private val baseUrl: String =
    servicesConfig.baseUrl("service-configs")

  def searchConfig(
    key           : String,
    value         : String,
    environment   : Seq[Environment],
    team          : Option[String],
    digitalService: Option[String]
  )(using HeaderCarrier): Future[Seq[Config]] =
    val params =
      Map(
        "key"                -> key,
        "value"              -> value,
        "showEnvironments[]" -> environment,
        "keyFilterType"      -> "equalTo",
        "valueFilterType"    -> "equalTo",
        "teamName"           -> team,
        "digitalService"     -> digitalService
      )

    given Reads[Config] = Config.reads

    httpClientV2
      .get(url"$baseUrl/service-configs/search?$params")
      .execute[Seq[Config]]

object ServiceConfigsConnector:

  case class Config(
    serviceName: String
  )

  object Config:
    val reads: Reads[Config] =
      (__ \ "serviceName").read[String].map(Config.apply)
