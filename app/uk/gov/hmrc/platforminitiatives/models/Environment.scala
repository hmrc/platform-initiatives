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

package uk.gov.hmrc.platforminitiatives.models

import play.api.mvc.{PathBindable, QueryStringBindable}

sealed trait Environment { def asString: String; def displayString: String }

object Environment {
  case object Development   extends Environment { val asString = "development";   override def displayString = "Development"   }
  case object Integration   extends Environment { val asString = "integration";   override def displayString = "Integration"   }
  case object QA            extends Environment { val asString = "qa";            override def displayString = "QA"            }
  case object Staging       extends Environment { val asString = "staging";       override def displayString = "Staging"       }
  case object ExternalTest  extends Environment { val asString = "externaltest";  override def displayString = "External Test" }
  case object Production    extends Environment { val asString = "production";    override def displayString = "Production"    }

  val values: List[Environment] =
    List(Development, Integration, QA, Staging, ExternalTest, Production)

  implicit val ordering = new Ordering[Environment] {
    def compare(x: Environment, y: Environment): Int =
      values.indexOf(x).compare(values.indexOf(y))
  }

  def parse(s: String): Option[Environment] =
    values.find(_.asString == s)
}

trait SlugInfoFlag  { def asString: String; def displayString: String }
object SlugInfoFlag {
  case object Latest extends SlugInfoFlag { override def asString = "latest"; override def displayString = "Latest" }
  case class ForEnvironment(env: Environment) extends SlugInfoFlag { override def asString = env.asString; override def displayString = env.displayString }

  val values: List[SlugInfoFlag] =
    Latest :: Environment.values.map(ForEnvironment.apply)

  implicit val ordering = new Ordering[SlugInfoFlag] {
    def compare(x: SlugInfoFlag, y: SlugInfoFlag): Int =
      values.indexOf(x).compare(values.indexOf(y))
  }

  def parse(s: String): Option[SlugInfoFlag] =
    values.find(_.asString == s)
}

