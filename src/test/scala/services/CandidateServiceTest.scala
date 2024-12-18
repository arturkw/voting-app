package services

import com.typesafe.config.ConfigFactory
import config.DbConfig
import model.{CandidateEntity, CandidateInDto}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class CandidateServiceTest extends AnyFunSuite with ScalaFutures with Matchers with BeforeAndAfterAll {

  var db: Database = null
  var candidateService: CandidateService = null
  implicit val ex: ExecutionContextExecutor = ExecutionContext.global

  override def beforeAll(): Unit = {
    val config = ConfigFactory.load()
    val db = Database.forConfig("postgresDB")
    candidateService = new CandidateService(db)
    val voterService = new VoterService(db)
    val votesService = new VotesService(db)
    val dbConfig = new DbConfig(candidateService, voterService, votesService, config)
    dbConfig.setupDb()
  }

  test("select * returns 1 candidate") {
    val future = candidateService.getAllCandidates()
    Thread.sleep(1000)
    whenReady(future) {
      result => result.nonEmpty
    }
  }

  test("add candidate returns entity id") {
    val future = candidateService.registerCandidate(CandidateInDto("George", "Washington"))
    whenReady(future) {
      result => result.firstName shouldBe "George"
    }
  }

}
