package services

import com.typesafe.config.ConfigFactory
import config.DbConfig
import model.{CandidateInDto, CandidateOutDto, VoterInDto, VoterOutDto}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers
import slick.jdbc.H2Profile.api._

import scala.concurrent._
import scala.concurrent.duration._

class VotesServiceTest extends AnyFunSuite with ScalaFutures with Matchers with BeforeAndAfterAll {

  implicit val ex = ExecutionContext.global
  var db: Database = null
  var candidateService: CandidateService = null
  var votesService: VotesService = null
  var voterService: VoterService = null

  override def beforeAll(): Unit = {
    val config = ConfigFactory.load()
    val db = Database.forConfig("postgresDB")
    candidateService = new CandidateService(db)
    voterService = new VoterService(db)
    votesService = new VotesService(db)
    val dbConfig = new DbConfig(candidateService, voterService, votesService, config)
    dbConfig.setupDb()
  }

  test("the voter is only able to cast a vote once") {
    //given:
    val candidateOutDto = Await.result(candidateService.registerCandidate(CandidateInDto("John", "Adams")), 5.second)
    val voterOutDto = Await.result(voterService.registerVoter(VoterInDto("Jan", "Nowak")), 5.second)
    //when:
    val castVoteFutures = for (i <- 1 to 10) yield votesService.castVote(voterOutDto.id, candidateOutDto.id)
    val combinedFuture = Future.sequence(castVoteFutures)
    Await.result(combinedFuture, 5.second)

    //then:
    val voters = Await.result(voterService.allVoters(), 5.second)
    val candidates = Await.result(candidateService.getAllCandidates(), 5.second)

    assert(isTransactionCorrect(voters, voterOutDto, candidates, candidateOutDto))
  }

  def isTransactionCorrect(voters: Seq[VoterOutDto], voterOutDto: VoterOutDto,
                           candidates: Seq[CandidateOutDto], candidateOutDto: CandidateOutDto): Boolean = {
    voters.size === 1 &&
      voters.head.id === voterOutDto.id &&
      voters.head.hasVoted === true &&
      candidates.size === 1 &&
      candidates.head.id === candidateOutDto.id &&
      candidates.head.votes === 1
  }

}
