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

import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TeamsAndRepositoriesConnector @Inject()(
  httpClientV2  : HttpClientV2,
  servicesConfig: ServicesConfig
)(implicit
  ec: ExecutionContext
) {
  private val teamsAndServicesBaseUrl: String =
    servicesConfig.baseUrl("teams-and-repositories")

  def allDefaultBranches()(implicit hc: HeaderCarrier): Future[Seq[RepositoryDisplayDetails]] = {
    implicit val rddr: Reads[RepositoryDisplayDetails] = RepositoryDisplayDetails.reads
    httpClientV2
      .get(url"$teamsAndServicesBaseUrl/api/repositories")
      .execute[Seq[RepositoryDisplayDetails]]
  }
}

case class RepositoryDisplayDetails(
  name          : String,
  createdAt     : Instant,
  lastUpdatedAt : Instant,
  isArchived    : Boolean,
  teamNames     : Seq[String],
  defaultBranch : String
)

object RepositoryDisplayDetails {
  val reads: Reads[RepositoryDisplayDetails] =
    ( (__ \ "name"         ).read[String]
    ~ (__ \ "createdAt"    ).read[Instant]
    ~ (__ \ "lastUpdatedAt").read[Instant]
    ~ (__ \ "isArchived"   ).read[Boolean]
    ~ (__ \ "teamNames"    ).read[Seq[String]]
    ~ (__ \ "defaultBranch").read[String]
    )(RepositoryDisplayDetails.apply _)
}
