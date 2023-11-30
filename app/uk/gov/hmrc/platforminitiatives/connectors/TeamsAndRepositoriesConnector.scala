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

package uk.gov.hmrc.platforminitiatives.connectors

import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class RepositoryDisplayDetails(
   name          : String,
   createdAt     : LocalDateTime,
   lastUpdatedAt : LocalDateTime,
   isArchived    : Boolean,
   teamNames     : Seq[String],
   defaultBranch : String
 )

object RepositoryDisplayDetails {
  val format: OFormat[RepositoryDisplayDetails] = {
    Json.format[RepositoryDisplayDetails]
  }
}

class TeamsAndRepositoriesConnector @Inject()(http: HttpClient, servicesConfig: ServicesConfig)(implicit val ec: ExecutionContext) {
  private implicit val rddf: OFormat[RepositoryDisplayDetails] = RepositoryDisplayDetails.format

  private val teamsAndServicesBaseUrl: String =
    servicesConfig.baseUrl("teams-and-repositories")

  def allDefaultBranches(implicit hc: HeaderCarrier): Future[Seq[RepositoryDisplayDetails]] =
    http.GET[Seq[RepositoryDisplayDetails]](
      url"$teamsAndServicesBaseUrl/api/repositories"
    )
}

object TeamsAndRepositoriesConnector {
  case class ServiceName(name: String) extends AnyVal
}
