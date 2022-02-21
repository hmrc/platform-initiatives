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

package uk.gov.hmrc.platforminitiatives.controllers

import play.api.libs.json.Json
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import uk.gov.hmrc.platforminitiatives.services.PlatformInitiativesService

@Singleton
class PlatformInitiativesController @Inject()(
   platformInitiativesService       :   PlatformInitiativesService,
   cc                               :   ControllerComponents
 )(implicit val ec: ExecutionContext)
  extends BackendController(cc) {

  implicit val hc = HeaderCarrier()

  def allInitiatives: Action[AnyContent] = {
    Action.async {
      platformInitiativesService.allPlatformInitiatives()
        .map(initiative =>
          Ok(Json.toJson(initiative)))
    }
  }
  def teamInitiatives(team: String): Action[AnyContent] = {
    Action.async {
      platformInitiativesService.allPlatformInitiatives(Option(team))
        .map(initiative =>
          Ok(Json.toJson(initiative.filter(_.progress.target != 0))))
    }
  }
}
