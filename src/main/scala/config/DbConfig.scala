package config

import com.typesafe.config.Config
import services.{CandidateService, VoterService, VotesService}

import scala.concurrent.Await
import scala.concurrent.duration._

class DbConfig(candidateService: CandidateService, voterService: VoterService, votesService: VotesService, config: Config) {

  def setupDb(): Unit = {
    Await.result(candidateService.createTableIfNotExist(), 2.seconds)
    Await.result(voterService.createTableIfNotExist(), 2.seconds)
    Await.result(votesService.createTableIfNotExist(), 2.seconds)

    if (config.getBoolean("db.drop.data")) {
      Await.result(votesService.dropData(), 2.seconds)
      Await.result(candidateService.dropData(), 2.seconds)
      Await.result(voterService.dropData(), 2.seconds)
    }
  }

}
