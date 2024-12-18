package model

import slick.lifted.Tag
import slick.jdbc.H2Profile.api._

case class CandidateInDto(firstName: String, lastName: String)

case class CandidateOutDto(id: Long, firstName: String, lastName: String, votes: Int)

case class VoterOutDto(id: Long, firstName: String, lastName: String, hasVoted: Boolean)

case class VoterInDto(firstName: String, lastName: String)

case class VoteInDto(voterId: Long, candidateId: Long)

case class CandidateEntity(id: Option[Long], firstName: String, lastName: String)

case class VoterEntity(id: Option[Long], firstName: String, lastName: String, hasVoted: Boolean = false)

case class VoteEntity(voterId: Long, candidateId: Long)

class Candidates(tag: Tag) extends Table[CandidateEntity](tag, "candidates") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def firstName = column[String]("first_name")

  def lastName = column[String]("last_name")

  def * = (id.?, firstName, lastName) <> (CandidateEntity.tupled, CandidateEntity.unapply)
}

class Voters(tag: Tag) extends Table[VoterEntity](tag, "voters") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def firstName = column[String]("first_name")

  def lastName = column[String]("last_name")

  def hasVoted: Rep[Boolean] = column[Boolean]("has_voted")

  def * = (id.?, firstName, lastName, hasVoted) <> (VoterEntity.tupled, VoterEntity.unapply)
}

class Votes(tag: Tag) extends Table[VoteEntity](tag, "votes") {
  def voterId = column[Long]("voter_id")

  def candidateId = column[Long]("candidate_id")

  def pk = primaryKey("pk_votes", (voterId, candidateId))

  def voterFk = foreignKey("fk_voter", voterId, TableQuery[Voters])(_.id)

  def candidateFk = foreignKey("fk_candidate", candidateId, TableQuery[Candidates])(_.id)

  def * = (voterId, candidateId) <> (VoteEntity.tupled, VoteEntity.unapply)
}