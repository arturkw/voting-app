package services

import model.{CandidateEntity, CandidateInDto, CandidateOutDto, Candidates}
import slick.jdbc.H2Profile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class CandidateService(db: Database) {

  private val candidates: TableQuery[Candidates] = TableQuery[Candidates]
  private implicit val ex: ExecutionContextExecutor = ExecutionContext.global

  def dropData(): Future[Int] = {
    db.run(candidates.delete)
  }

  def createTableIfNotExist(): Future[Int] = {
    val sql =
      sqlu"""
          create table if not exists "candidates" ("id" bigserial NOT NULL PRIMARY KEY, "first_name" VARCHAR NOT NULL,"last_name" VARCHAR NOT NULL)
          """
    db.run(sql)
  }

  def toDto(c: CandidateEntity): CandidateOutDto = {
    CandidateOutDto(c.id.get, c.firstName, c.lastName, 0)
  }

  def registerCandidate(candidate: CandidateInDto): Future[CandidateOutDto] = {
    val insertAction = (candidates returning candidates.map(_.id)) into ((candidate, id) => candidate.copy(id = Some(id))) += CandidateEntity(Option.empty, candidate.firstName, candidate.lastName)
    db.run(insertAction).map(c => toDto(c))
  }

  def getAllCandidates(): Future[Seq[CandidateOutDto]] = {
    val query = sql"""
        select c.*, count(v.candidate_id) from candidates c left join votes v on c.id = v.candidate_id group by c.id;
        """.as[(Long, String, String, Int)]

    val result: Future[Vector[(Long, String, String, Int)]] = db.run(query)

    result.map(_.map { case (uuid, firstName, lastName, count) => CandidateOutDto(
      id = uuid,
      firstName = firstName,
      lastName = lastName,
      votes = count)
    })
  }

}

