package routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import model.{VoterInDto, VoterOutDto}
import services.VoterService
import spray.json.DefaultJsonProtocol._
import spray.json.{RootJsonFormat, enrichAny}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class VoterRoutes(votersService: VoterService)(implicit ec: ExecutionContext) {

  implicit val voterOutFormat: RootJsonFormat[VoterOutDto] = jsonFormat4(VoterOutDto)
  implicit val voterInFormat: RootJsonFormat[VoterInDto] = jsonFormat2(VoterInDto)

  val routes: Route =
    pathPrefix("voter") {
      path("all") {
        get {
          complete {
            votersService.allVoters().map(e => e.toJson.prettyPrint).map { result =>
              HttpResponse(
                status = StatusCodes.OK,
                entity = HttpEntity(ContentTypes.`application/json`, result)
              )
            }.recover {
              case ex: Exception =>
                HttpResponse(
                  status = StatusCodes.InternalServerError,
                  entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Error: ${ex.getMessage}")
                )
            }
          }
        }
      } ~ post {
        entity(as[VoterInDto]) { voter =>
          onComplete(votersService.registerVoter(voter)) {
            case Success(registeredVoter) => complete(HttpResponse(StatusCodes.OK, entity = HttpEntity.apply(registeredVoter.toJson.prettyPrint)))
            case Failure(exception) => complete(StatusCodes.InternalServerError, s"Error: ${exception.getMessage}")
          }
        }
      }
    }

}
