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

package de.heikoseeberger.akkahttpjson4s

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.{ ActorFlowMaterializer, FlowMaterializer }
import scala.concurrent.ExecutionContext
import scala.io.StdIn
import Json4sSupport.{ json4sMarshaller, json4sUnmarshaller }
import org.json4s._

object ExampleApp {

  case class Foo(bar: String)

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val mat = ActorFlowMaterializer()
    import system.dispatcher

    Http().bindAndHandle(route, "127.0.0.1", 8080)

    StdIn.readLine("Hit ENTER to exit")
    system.shutdown()
    system.awaitTermination()
  }

  def route(implicit ec: ExecutionContext, mat: FlowMaterializer) = {
    import Directives._
    implicit val serialization = jackson.Serialization // or native.Serialization

    path("") {
      post {
        entity(as[Foo]) { foo =>
          complete {
            foo
          }
        }
      }
    }
  }
}