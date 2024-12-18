package services

import model.Votes
import slick.jdbc.H2Profile.api._
import slick.lifted.TableQuery

import scala.concurrent.Future

class VotesService(db: Database) {

  val votes = TableQuery[Votes]

  def createTableIfNotExist(): Future[Int] = {

    val sql =
      sqlu"""
        create table if not exists votes (
            voter_id bigint not null constraint fk_voter references public.voters,
            candidate_id bigint not null constraint fk_candidate references public.candidates,
            constraint pk_votes primary key (voter_id, candidate_id)
      );
        """
    db.run(sql)
  }

  def dropData(): Future[Int] = {
    db.run(votes.delete)
  }

  def castVote(voterId: Long, candidateId: Long): Future[Int] = {
    val updateQuery =
      sqlu"""
      BEGIN;

      UPDATE voters
      SET has_voted = TRUE
      WHERE id = $voterId
        AND has_voted = FALSE;

      INSERT INTO votes (voter_id, candidate_id)
      SELECT $voterId, $candidateId
      WHERE NOT EXISTS(
              SELECT 1 FROM votes WHERE voter_id = $voterId AND candidate_id = $candidateId
          );

      COMMIT;
      """

    db.run(updateQuery.transactionally)
  }

}

