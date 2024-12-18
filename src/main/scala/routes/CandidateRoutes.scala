package routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import model.{CandidateInDto, CandidateOutDto}
import services.CandidateService
import spray.json.DefaultJsonProtocol._
import spray.json.{RootJsonFormat, enrichAny}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}

class CandidateRoutes(candidateService: CandidateService) {

  private implicit val candidateFormat: RootJsonFormat[CandidateInDto] = jsonFormat2(CandidateInDto)
  private implicit val candidateOutFormat: RootJsonFormat[CandidateOutDto] = jsonFormat4(CandidateOutDto)
  private implicit val ex: ExecutionContextExecutor = ExecutionContext.global

  val routes: Route = {
    pathPrefix("candidate") {
      path("all") {
        get {
          complete {
            candidateService.getAllCandidates().map(e => e.toJson.prettyPrint).map { result =>
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
        entity(as[CandidateInDto]) { candidate =>
          onComplete(candidateService.registerCandidate(candidate)) {
            case Success(registeredCandidate) => complete(HttpResponse(StatusCodes.OK, entity = HttpEntity.apply(registeredCandidate.toJson.prettyPrint)))
            case Failure(exception) => complete(StatusCodes.InternalServerError, s"Error: ${exception.getMessage}")
          }
        }
      }
    }
  }

}
