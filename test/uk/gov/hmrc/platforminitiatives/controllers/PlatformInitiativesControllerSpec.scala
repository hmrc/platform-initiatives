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

package uk.gov.hmrc.platforminitiatives.controllers

import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status, stubControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.platforminitiatives.models.PlatformInitiative
import uk.gov.hmrc.platforminitiatives.services.PlatformInitiativesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps


class PlatformInitiativesControllerSpec
  extends AnyWordSpec
    with Matchers
    with Results
    with MockitoSugar {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "Platform Initiatives controller" should {
    "have the correct url set up for the initiatives list" in {
      uk.gov.hmrc.platforminitiatives.controllers.routes.PlatformInitiativesController.allInitiatives()
        .url mustBe "/initiatives"
    }
  }

  "PlatformInitiativesController.allInitiatives" should {
    "Return a 200 status code and correct JSON for PlatformInitiatives" in new Setup {
      val mockInitiatives: Seq[PlatformInitiative] = Seq(
        PlatformInitiative(
          initiativeName = "Test initiative",
          initiativeDescription = "Test initiative description",
          currentProgress       = 10,
          targetProgress        = 100,
          completedLegend       = "Completed",
          inProgressLegend      = "Not completed"
        ),
        PlatformInitiative(
          initiativeName        = "Update Dependency",
          initiativeDescription = "Update Dependency description",
          currentProgress       = 50,
          targetProgress        = 70,
          completedLegend       = "Completed",
          inProgressLegend      = "Not completed"
        )
      )
      when(mockPlatformInitiativesService.allPlatformInitiatives) thenReturn {
        Future.successful(mockInitiatives)
      }
      val result     : Future[Result]          = controller.allInitiatives.apply(FakeRequest())
      val initiatives: JsValue = contentAsJson(result)
      println(initiatives)
      status(result) mustBe 200
      initiatives mustBe Json.parse(
        """
          [{
             "initiativeName"        : "Test initiative",
             "initiativeDescription" : "Test initiative description",
             "currentProgress"       : 10,
             "targetProgress"        : 100,
             "completedLegend"       : "Completed",
             "inProgressLegend"      : "Not completed"
           },
           {
             "initiativeName"        : "Update Dependency",
             "initiativeDescription" : "Update Dependency description",
             "currentProgress"       : 50,
             "targetProgress"        : 70,
             "completedLegend"       : "Completed",
             "inProgressLegend"      : "Not completed"
           }
          ]
          """)
    }
  }

  private trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockPlatformInitiativesService: PlatformInitiativesService = mock[PlatformInitiativesService]
    val controller = new PlatformInitiativesController(
        mockPlatformInitiativesService,
        stubControllerComponents()
    )
  }
}
