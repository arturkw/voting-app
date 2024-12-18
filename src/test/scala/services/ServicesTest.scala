package services

import com.typesafe.config.ConfigFactory
import config.DbConfig
import model.{CandidateEntity, CandidateInDto, CandidateOutDto, VoteEntity, VoterEntity, VoterInDto, VoterOutDto}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import slick.jdbc.H2Profile.api._

import scala.concurrent._
import scala.concurrent.duration._


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


  private def isTransactionCorrect(voters: Seq[VoterOutDto], votes: Seq[VoteEntity]): Boolean = {
    votes.size === 1 &&
      voters.size === 1 &&
      votes.head.voterId === 1 &&
      votes.head.candidateId === 1 &&
      voters.head.id === 1 &&
      voters.head.hasVoted === true
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
