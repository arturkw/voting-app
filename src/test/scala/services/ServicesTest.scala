package services

import com.typesafe.config.ConfigFactory
import config.DbConfig
import model.{CandidateInDto, CandidateOutDto}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import slick.jdbc.H2Profile.api._

import scala.concurrent._

class ServicesTest extends AnyFunSuite with ScalaFutures with Matchers with BeforeAndAfterAll {

  implicit val ex: ExecutionContextExecutor = ExecutionContext.global
  var db: Database = null
  var candidateService: CandidateService = null
  var votesService: VotesService = null
  var voterService: VoterService = null

  override def beforeAll(): Unit = {
    val config = ConfigFactory.load()
    val db = Database.forConfig("postgresDB")
    candidateService = new CandidateService(db)
    val voterService = new VoterService(db)
    val votesService = new VotesService(db)
    val dbConfig = new DbConfig(candidateService, voterService, votesService, config)
    dbConfig.setupDb()
  }

  test("add candidate returns entity id") {
    val future = candidateService.registerCandidate(CandidateInDto("George", "Washington"))
    whenReady(future) {
      result => isValid(result) shouldBe true
    }
  }

  def isValid(result: CandidateOutDto): Boolean = {
    result.firstName === "George"
    result.lastName === "Washington"
  }


}
