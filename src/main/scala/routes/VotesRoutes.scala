package routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import model.VoteInDto
import services.VotesService
import spray.json.RootJsonFormat

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}
import spray.json.DefaultJsonProtocol._

class VotesRoutes(votesService: VotesService) {
  private implicit val ex: ExecutionContextExecutor = ExecutionContext.global
  private implicit val voteFormat: RootJsonFormat[VoteInDto] = jsonFormat2(VoteInDto)

  val routes: Route =
    path("vote") {
      post {
        entity(as[VoteInDto]) { vote =>
          // Wywołanie serwisu VotesService
          onComplete(votesService.castVote(vote.voterId, vote.candidateId)) {
            case Success(value) =>
              complete(HttpResponse(StatusCodes.OK, entity = HttpEntity.apply(s"Voter with id = ${vote.voterId} cast a vote for candidate ${vote.candidateId}")))
            case Failure(exception) =>
              complete(StatusCodes.InternalServerError, s"Error: ${exception.getMessage}")
          }
        }
      }
    }


}
