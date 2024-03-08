/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.platforminitiatives.models

sealed trait DependencyScope {
  def asString: String
}

object DependencyScope {
  case object Compile extends DependencyScope {
    override val asString = "compile"
  }

  case object Provided extends DependencyScope {
    override val asString = "provided"
  }

  case object Test extends DependencyScope {
    override val asString = "test"
  }

  case object It extends DependencyScope {
    override val asString = "it"
  }

  case object Build extends DependencyScope {
    override val asString = "build"
  }

  val values: List[DependencyScope] =
    List(Compile, Provided, Test, It, Build)
}