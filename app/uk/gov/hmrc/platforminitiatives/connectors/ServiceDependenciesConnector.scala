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

import play.api.cache.AsyncCacheApi
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, StringContextOps}
import uk.gov.hmrc.platforminitiatives.models._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ServiceDependenciesConnector @Inject() (
  httpClient      : HttpClient,
  servicesConfig  : ServicesConfig,
  cache           : AsyncCacheApi
)(implicit ec: ExecutionContext)  {

  private val servicesDependenciesBaseUrl: String =
    servicesConfig.baseUrl("service-dependencies")

  private val cacheExpiration: Duration =
    servicesConfig
      .getDuration("microservice.services.service-dependencies.cache.expiration")

  def getAllDependencies()(implicit hc: HeaderCarrier): Future[Seq[Dependencies]] = {
    import Dependencies.Implicits.reads
    cache.getOrElseUpdate("service-dependencies", cacheExpiration) {
      httpClient.GET[Seq[Dependencies]](url"$servicesDependenciesBaseUrl/api/dependencies")
    }
  }

  def getMetaArtefactDependency(
    group       : String,
    artefact    : String,
    environment : Option[Environment],
    scopes      : List[DependencyScope],
    range       : String = "[0.0.0,)"
  )(implicit hc: HeaderCarrier): Future[Seq[MetaArtefactDependency]] = {
    val repoType = if (environment.isDefined) List("Service") else List("Service", "Library", "Test", "Other")
    httpClient.GET[Seq[MetaArtefactDependency]](
      url"$servicesDependenciesBaseUrl/api/repoDependencies?group=$group&artefact=$artefact&versionRange=$range&flag=$environment&scope=${scopes.map(_.asString)}&repoType=$repoType"
    )
  }

  def getSlugJdkVersions(team: Option[String])(implicit hc: HeaderCarrier): Future[Seq[SlugJdkVersion]] =
    httpClient.GET[Seq[SlugJdkVersion]](
      url"$servicesDependenciesBaseUrl/api/jdkVersions?team=$team"
    )
}
