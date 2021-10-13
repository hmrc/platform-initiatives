/*
 * Copyright 2021 HM Revenue & Customs
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

import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, StringContextOps}
import uk.gov.hmrc.platforminitiatives.models.Dependencies
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ServiceDependenciesConnector @Inject() (
  http          : HttpClient,
  servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext)  {

  private val servicesDependenciesBaseUrl: String =
    servicesConfig.baseUrl("service-dependencies")

  def getAllDependencies()(implicit hc: HeaderCarrier): Future[Seq[Dependencies]] = {
    import Dependencies.Implicits.reads
    // Todo: find out how to populate the below locally for development.
    http.GET[Seq[Dependencies]](url"$servicesDependenciesBaseUrl/api/dependencies")
  }

  def getAllBobbyRules(dependencies: Future[Seq[Dependencies]])(implicit hc: HeaderCarrier): Unit = {
//    dependencies.map(a => a.map(_.libraryDependencies.map(_)))
  }
}
