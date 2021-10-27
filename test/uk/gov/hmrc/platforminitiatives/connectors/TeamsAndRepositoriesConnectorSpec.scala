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

import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.Success

class TeamsAndRepositoriesConnectorSpec
  extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "getAllRepositories" should {
    "return a future sequence of RepositoryDisplayDetails" in new Setup {
      when(mockTeamsAndRepositoriesConnector.allDefaultBranches) thenReturn
        Future.successful(mockRepositories)
      val repositories: Future[Seq[RepositoryDisplayDetails]] = mockTeamsAndRepositoriesConnector.allDefaultBranches
      repositories.onComplete({
        case Success(value) => {
          value.length shouldBe 4
        }
      })
    }
  }

  private[this] trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockTeamsAndRepositoriesConnector: TeamsAndRepositoriesConnector = mock[TeamsAndRepositoriesConnector]
    val mockRepositories: Seq[RepositoryDisplayDetails] = Seq(
      RepositoryDisplayDetails(
        name = "test",
        createdAt = LocalDateTime.now(),
        lastUpdatedAt = LocalDateTime.now(),
        isArchived = false,
        teamNames = Seq("Team-1", "Team-2"),
        defaultBranch = "main"
      ),
      RepositoryDisplayDetails(
        name = "test-2",
        createdAt = LocalDateTime.now(),
        lastUpdatedAt = LocalDateTime.now(),
        isArchived = false,
        teamNames = Seq("Team-1"),
        defaultBranch = "master"
      ),
      RepositoryDisplayDetails(
        name = "test-3",
        createdAt = LocalDateTime.now(),
        lastUpdatedAt = LocalDateTime.now(),
        isArchived = false,
        teamNames = Seq("Team-1"),
        defaultBranch = "main"
      ),
      RepositoryDisplayDetails(
        name = "test-4",
        createdAt = LocalDateTime.now(),
        lastUpdatedAt = LocalDateTime.now(),
        isArchived = true,
        teamNames = Seq("Team-2"),
        defaultBranch = "master"
      )
    )
  }
}
