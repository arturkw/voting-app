package main

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import config.DbConfig
import org.slf4j.{Logger, LoggerFactory}
import routes.{CandidateRoutes, VoterRoutes, VotesRoutes}
import services.{CandidateService, VoterService, VotesService}
import slick.jdbc.H2Profile.api._

object Main {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val db = Database.forConfig("postgresDB")
    val candidateService = new CandidateService(db)
    val voterService = new VoterService(db)
    val votesService = new VotesService(db)
    val dbConfig = new DbConfig(candidateService, voterService, votesService, config)
    val candidateRoutes: CandidateRoutes = new CandidateRoutes(candidateService)
    val votesRoutes: VotesRoutes = new VotesRoutes(votesService)
    val voterRoutes: VoterRoutes = new VoterRoutes(voterService)

    val route: Route = pathPrefix("api") {
      concat(
        candidateRoutes.routes,
        votesRoutes.routes,
        voterRoutes.routes
      )
    }
    new Main(dbConfig, route).start()
  }
}

class Main(dbConfig: DbConfig, route: Route) {
  private implicit val system: ActorSystem = ActorSystem("rest-system")
  private val logger: Logger = LoggerFactory.getLogger(Main.getClass)

  private def start(): Unit = {
    dbConfig.setupDb()
    Http().newServerAt("localhost", 8080).bind(route)
    logger.info("Server is running at http://localhost:8080")
  }

}