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
    "Return 200 status for PlatformInitiatives" in new Setup {
      when(mockPlatformInitiativesService.allPlatformInitiatives) thenReturn {
        Future.successful(mockInitiatives)
      }
      val result: Future[Result] = controller.allInitiatives.apply(FakeRequest())
      status(result) mustBe 200
    }
  }

  "PlatformInitiativesController.allInitiatives" should {
    "Return correct JSON for PlatformInitiatives" in new Setup {
      when(mockPlatformInitiativesService.allPlatformInitiatives) thenReturn {
        Future.successful(mockInitiatives)
      }
      val result     : Future[Result]          = controller.allInitiatives.apply(FakeRequest())
      val initiatives: Seq[PlatformInitiative] = contentAsJson(result).as[Seq[PlatformInitiative]]
      initiatives.map(_.initiativeName) mustBe
        Seq(
          "Test initiative", "Update Dependency 1", "Update Dependency 2", "Update Dependency 3"
        )
      initiatives.map(_.currentProgress) mustBe Seq(10, 50, 10, 50)
    }
  }

  private trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockPlatformInitiativesService: PlatformInitiativesService = mock[PlatformInitiativesService]
    val mockInitiatives: Seq[PlatformInitiative] = Seq(
        PlatformInitiative(
        initiativeName        = "Test initiative",
        initiativeDescription = "Test initiative description",
        currentProgress       = 10,
        targetProgress        = 100,
        completedLegend       = "Completed",
        inProgressLegend      = "Not completed"
    ),
        PlatformInitiative(
        initiativeName        = "Update Dependency 1",
        initiativeDescription = "Update Dependency 1 description",
        currentProgress       = 50,
        targetProgress        = 70,
        completedLegend       = "Completed",
        inProgressLegend      = "Not completed"
    ),
        PlatformInitiative(
        initiativeName        = "Update Dependency 2",
        initiativeDescription = "Update Dependency 2 description",
        currentProgress       = 10,
        targetProgress        = 50,
        completedLegend       = "Completed",
        inProgressLegend      = "Not completed"
    ),
      PlatformInitiative(
        initiativeName        = "Update Dependency 3",
        initiativeDescription = "Update Dependency 3 description",
        currentProgress       = 50,
        targetProgress        = 50,
        completedLegend       = "Completed",
        inProgressLegend      = "Not completed"
      )
    )

    val controller = new PlatformInitiativesController(
        mockPlatformInitiativesService,
        stubControllerComponents()
    )
  }
}
