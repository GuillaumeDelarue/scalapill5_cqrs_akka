package scalapills.cqrs.samples

import akka.actor.{Actor, ActorLogging}

import scalapills.cqrs.domain.{Increment, Report, Result}

class BasicCounter extends Actor with ActorLogging {
  private var count = 0

  def receive = {
    case Increment => count += 1
    case Report =>
      log.info(s"Current count: $count")
      sender ! Result(count)
    case unknown => log.error(s"Received unknown message: [$unknown]")
  }
}
