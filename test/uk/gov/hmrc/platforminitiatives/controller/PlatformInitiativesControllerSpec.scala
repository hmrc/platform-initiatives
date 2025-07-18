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

package uk.gov.hmrc.platforminitiatives.controller

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status, stubControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.platforminitiatives.model.{PlatformInitiative, Progress}
import uk.gov.hmrc.platforminitiatives.service.PlatformInitiativesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class PlatformInitiativesControllerSpec
  extends AnyWordSpec
     with Matchers
     with Results
     with MockitoSugar:

  "Platform Initiatives controller" should:
    "have the correct url set up for the initiatives list" in:
      uk.gov.hmrc.platforminitiatives.controller.routes.PlatformInitiativesController.initiatives(None, None)
        .url shouldBe "/initiatives"

  "PlatformInitiativesController.allInitiatives" should:
    "return a 200 status code and correct JSON for PlatformInitiatives" in new Setup:
      val initiatives: Seq[PlatformInitiative] =
        Seq(
          PlatformInitiative(
            initiativeName        = "Test initiative",
            initiativeDescription = "Test initiative description",
            progress              = Progress(
                                      current       = 10,
                                      target        = 100
                                    ),
            completedLegend       = "Completed",
            inProgressLegend      = "Not completed",
            experimental          = false
          ),
          PlatformInitiative(
            initiativeName        = "Update Dependency",
            initiativeDescription = "Update Dependency description",
            progress              = Progress(
                                      current       = 50,
                                      target        = 70
                                    ),
            completedLegend       = "Completed",
            inProgressLegend      = "Not completed",
            experimental          = false
          )
        )

      when(mockPlatformInitiativesService.platformInitiatives(None, None))
        .thenReturn(Future.successful(initiatives))

      val result: Future[Result] = controller.initiatives(None, None).apply(FakeRequest())

      status(result)        shouldBe 200
      contentAsJson(result) shouldBe Json.parse(
        """
          [{
             "initiativeName"        : "Test initiative",
             "initiativeDescription" : "Test initiative description",
             "progress"              : {
                "current"       : 10,
                "target"        : 100
             },
             "completedLegend"       : "Completed",
             "inProgressLegend"      : "Not completed"
           },
           {
             "initiativeName"        : "Update Dependency",
             "initiativeDescription" : "Update Dependency description",
             "progress"              : {
                "current"       : 50,
                "target"        : 70
             },
             "completedLegend"       : "Completed",
             "inProgressLegend"      : "Not completed"
           }
          ]
          """)

  "PlatformInitiativesController.teamInitiatives" should:
    "return a 200 status code and correct JSON for a specified teams PlatformInitiatives" in new Setup:
      val initiatives: Seq[PlatformInitiative] =
        Seq(
          PlatformInitiative(
            initiativeName        = "Test initiative",
            initiativeDescription = "Test initiative description",
            progress              = Progress(
                                      current       = 1,
                                      target        = 1
                                    ),
            completedLegend       = "Completed",
            inProgressLegend      = "Not completed",
            experimental          = false
          ),
          PlatformInitiative(
            initiativeName        = "Update Dependency",
            initiativeDescription = "Update Dependency description",
            progress              = Progress(
                                      current       = 0,
                                      target        = 1
                                    ),
            completedLegend       = "Completed",
            inProgressLegend      = "Not completed",
            experimental          = false
          )
        )

      when(mockPlatformInitiativesService.platformInitiatives(Some("team-1"), None))
        .thenReturn(Future.successful(initiatives))

      val result: Future[Result] = controller.initiatives(Some("team-1"), None).apply(FakeRequest())

      status(result)        shouldBe 200
      contentAsJson(result) shouldBe Json.parse(
        """
          [{
             "initiativeName"        : "Test initiative",
             "initiativeDescription" : "Test initiative description",
             "progress"              : {
                "current"    : 1,
                "target"     : 1
             },
             "completedLegend"       : "Completed",
             "inProgressLegend"      : "Not completed"
           },
           {
             "initiativeName"        : "Update Dependency",
             "initiativeDescription" : "Update Dependency description",
             "progress"              : {
                 "current"   : 0,
                 "target"    : 1
             },
             "completedLegend"       : "Completed",
             "inProgressLegend"      : "Not completed"
           }
          ]
          """)

  private trait Setup:
    given HeaderCarrier = HeaderCarrier()

    val mockPlatformInitiativesService: PlatformInitiativesService = mock[PlatformInitiativesService]

    val controller = new PlatformInitiativesController(
      mockPlatformInitiativesService,
      stubControllerComponents()
    )
