/*
 * Copyright 2015 Heiko Seeberger
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

package de.heikoseeberger.akkahttpcirce

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ HttpEntity, MediaTypes, RequestEntity }
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, DurationInt }

object CirceSupportSpec {
  case class Foo(bar: String) { require(bar == "bar", "bar must be 'bar'!") }
}

class CirceSupportSpec extends WordSpec with Matchers with BeforeAndAfterAll {
  import CirceSupport._
  import CirceSupportSpec._

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  val foo = Foo("bar")

  "CirceSupport" should {
    import system.dispatcher

    "enable marshalling and unmarshalling objects for generic derivation" in {
      import io.circe.generic.auto._
      val entity = Await.result(Marshal(foo).to[RequestEntity], 100.millis)
      Await.result(Unmarshal(entity).to[Foo], 100.millis) shouldBe foo
    }

    "provide proper error messages for requirement errors" in {
      import io.circe.generic.auto._
      val entity = HttpEntity(MediaTypes.`application/json`, """{ "bar": "baz" }""")
      val iae = the[IllegalArgumentException] thrownBy Await.result(Unmarshal(entity).to[Foo], 100.millis)
      iae should have message "requirement failed: bar must be 'bar'!"
    }

  }

  override protected def afterAll() = {
    Await.ready(system.terminate(), Duration.Inf)
    super.afterAll()
  }
}
