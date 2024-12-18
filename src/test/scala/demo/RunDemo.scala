package demo

import model._
import spray.json.DefaultJsonProtocol._
import spray.json.{RootJsonFormat, enrichAny, _}

import java.io.{BufferedReader, InputStreamReader, OutputStream}
import java.net.{HttpURLConnection, URL}
import scala.io.StdIn.readLine
import scala.util.Random

object RunDemo {

  implicit val candidateInFormat: RootJsonFormat[CandidateInDto] = jsonFormat2(CandidateInDto)
  implicit val candidateOutFormat: RootJsonFormat[CandidateOutDto] = jsonFormat4(CandidateOutDto)
  implicit val voterInFormat: RootJsonFormat[VoterInDto] = jsonFormat2(VoterInDto)
  implicit val voterOutFormat: RootJsonFormat[VoterOutDto] = jsonFormat4(VoterOutDto)
  implicit val voteFormat: RootJsonFormat[VoteInDto] = jsonFormat2(VoteInDto)

  def main(args: Array[String]): Unit = {

    println("Starting Voting App demo...")
    println("This demo performs the following steps: ")
    println(" - Generates the specified number of candidates")
    println(" - Generates the specified number of voters")
    println(" - Randomly assigns a number of voters to candidates")
    println(" - Shows the voting result calculated locally in the Demo application")
    println(" - Shows the voting result retrieved from the API response")
    println("")
    println("Enter the number of candidates (max 10):")
    val candidatesCount = readLine().toInt
    if (candidatesCount > 10 || candidatesCount <= 0) {
      System.exit(-1)
    }
    println("Generated candidates: ")
    val candidates = for {_ <- 1 to candidatesCount
                          candidateFirstName = getRandomElement(presidentFirstNames)
                          candidateLastName = getRandomElement(presidentLastNames)
                          } yield {
      println(s" * $candidateFirstName $candidateLastName")
      CandidateInDto(candidateFirstName, candidateLastName)
    }
    println("Registering candidates: http://localhost:8080/api/candidate [POST]")
    val candidatesDto = for {candidate <- candidates
                             dto = post(candidate.toJson.prettyPrint, "http://localhost:8080/api/candidate").parseJson.convertTo[CandidateOutDto]
                             } yield dto

    println("Enter the number of voters (max 50):")
    val votersCount = readLine().toInt
    if (votersCount > 50 || votersCount <= 0) {
      System.exit(-1)
    }
    println("Generated voters: ")
    val voters = for {_ <- 1 to votersCount
                      voterFirstName = getRandomElement(firstNames)
                      voterLastName = getRandomElement(lastNames)
                      } yield {
      println(s" * $voterFirstName $voterLastName")
      CandidateInDto(voterFirstName, voterLastName)
    }
    println("Registering voters: http://localhost:8080/api/voters [POST]:")
    val votersDto = for {voter <- voters
                         dto = post(voter.toJson.prettyPrint, "http://localhost:8080/api/voter").parseJson.convertTo[VoterOutDto]
                         } yield dto

    println(s"Enter the number of votes to cast (max = ${votersDto.size}): ")
    val votesCount = readLine().toInt
    if (votesCount > votersDto.size) {
      System.exit(-1)
    }
    val randomVoters = Random.shuffle(votersDto).take(votesCount)
    val votesInDto = for {
      voter <- randomVoters
      candidate = getRandomElement[CandidateOutDto](candidatesDto)
      _ = println(s"Voter [${voter.id}: ${voter.firstName} ${voter.lastName}] votes for [${candidate.id}: ${candidate.firstName} ${candidate.lastName}]")
      vote = VoteInDto(voter.id, candidate.id)
    } yield vote

    for (voteInDto <- votesInDto) {
      post(voteInDto.toJson.prettyPrint, "http://localhost:8080/api/vote")
    }


    val votesCountByCandidateId = votesInDto.groupBy(_.candidateId).map { case (a, b) => a -> b.size }
    println("Expected voting result: ")
    for (a <- votesCountByCandidateId.toSeq.sortBy(_._1).toMap) {
      println(s"  Candidate id = ${a._1} received ${a._2} votes")
    }

    val dtoes = get("http://localhost:8080/api/candidate/all").parseJson.convertTo[Array[CandidateOutDto]]
    dtoes.groupBy(_.id).map { case (a, b) => a -> b.length }
    println("Voting result retrieved from API: ")
    for (a <- dtoes.toSeq.sortBy(_.id)) {
      println(s"  Candidate id = ${a.id} received ${a.votes} votes")
    }
  }

  def post(jsonInputString: String, url: String): String = {
    val obj = new URL(url)
    val con = obj.openConnection().asInstanceOf[HttpURLConnection]
    con.setRequestMethod("POST")
    con.setRequestProperty("Content-Type", "application/json")
    con.setDoOutput(true)

    val os: OutputStream = con.getOutputStream
    val input = jsonInputString.getBytes("utf-8")
    os.write(input, 0, input.length)
    val in = new BufferedReader(new InputStreamReader(con.getInputStream))
    var inputLine: String = null
    val response = new StringBuffer

    while ( {
      inputLine = in.readLine()
      inputLine != null
    }) {
      response.append(inputLine)
    }
    in.close()
    response.toString
  }

  def get(url: String): String = {
    val obj = new URL(url)
    val con = obj.openConnection().asInstanceOf[HttpURLConnection]
    con.setRequestMethod("GET")
    val in = new BufferedReader(new InputStreamReader(con.getInputStream))
    var inputLine: String = null
    val response = new StringBuffer

    while ( {
      inputLine = in.readLine()
      inputLine != null
    }) {
      response.append(inputLine)
    }
    in.close()
    response.toString
  }

  def getRandomElement[T](elements: Seq[T]): T = {
    elements(Random.nextInt(elements.length))
  }

  private val lastNames = Seq(
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "García", "Rodriguez", "Martínez",
    "Hernández", "Lopez", "González", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
    "Lee", "Perez", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Roberts", "Walker"
  )

  private val firstNames = Seq(
    "James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph", "Charles", "Thomas",
    "Christopher", "Daniel", "Paul", "Mark", "Donald", "George", "Kenneth", "Steven", "Edward", "Brian",
    "Ronald", "Anthony", "Kevin", "Jason", "Matthew", "Gary", "Timothy", "Jose", "Larry", "Jeffrey", "Frank"
  )

  private val presidentFirstNames = Seq(
    "George", "John", "Thomas", "James", "James", "Andrew", "Abraham", "Ulysses", "Rutherford", "James",
    "Grover", "Benjamin", "William", "Theodore", "Woodrow", "Warren", "Calvin", "Herbert", "Franklin", "Harry",
    "Dwight", "John", "Lyndon", "Richard", "Gerald", "Jimmy", "Ronald", "George", "Bill", "Barack", "Donald"
  )

  private val presidentLastNames = Seq(
    "Washington", "Adams", "Jefferson", "Madison", "Monroe", "Adams", "Jackson", "Van Buren", "Harrison", "Tyler",
    "Polk", "Taylor", "Fillmore", "Pierce", "Buchanan", "Lincoln", "Johnson", "Grant", "Hayes", "Garfield",
    "Arthur", "Cleveland", "Harrison", "Cleveland", "McKinley", "Roosevelt", "Taft", "Wilson", "Harding",
    "Coolidge", "Hoover", "Roosevelt", "Truman", "Eisenhower", "Kennedy", "Johnson", "Nixon", "Ford",
    "Carter", "Reagan", "Bush", "Clinton", "Bush", "Obama", "Trump", "Biden"
  )

}
