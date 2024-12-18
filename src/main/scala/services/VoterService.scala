package services

import model.{VoterEntity, VoterInDto, VoterOutDto, Voters}
import slick.jdbc.H2Profile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class VoterService(db: Database) {

  private val voters = TableQuery[Voters]
  private implicit val ex: ExecutionContextExecutor = ExecutionContext.global

  def dropData(): Future[Int] = {
    db.run(voters.delete)
  }

  def createTableIfNotExist(): Future[Int] = {
    val sql = sqlu"""create table if not exists "voters" ("id" bigserial NOT NULL PRIMARY KEY, first_name varchar not null, last_name  varchar not null, has_voted  boolean not null);"""
    db.run(sql)
  }

  def registerVoter(voterInDto: VoterInDto): Future[VoterOutDto] = {
    val insertAction = (voters returning voters.map(_.id)) into ((voter, id) => voter.copy(id = Some(id))) += VoterEntity(Option.empty, voterInDto.firstName, voterInDto.lastName)
    db.run(insertAction).map(voterEntity => toDto(voterEntity))
  }

  def allVoters(): Future[Seq[VoterOutDto]] = {
    val query = voters.result
    val result: Future[Seq[VoterEntity]] = db.run(query)
    result.map { voters => voters.map(voterEntity => toDto(voterEntity)) }
  }

  private def toDto(voterEntity: VoterEntity) = {
    VoterOutDto(voterEntity.id.get, voterEntity.firstName, voterEntity.lastName, voterEntity.hasVoted)
  }
}

