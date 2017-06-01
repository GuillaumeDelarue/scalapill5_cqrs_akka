package scalapills.cqrs.samples

import akka.actor.ActorLogging
import akka.persistence.PersistentActor

import scalapills.cqrs.domain.{CountIncrementRequested, Increment, Report, Result}

class EventSourcingCounter(override val persistenceId: String) extends PersistentActor with ActorLogging {
  private var count = 0

  // Command handler
  override def receiveCommand: Receive = {
    case Increment =>
      persist(CountIncrementRequested(count)) { event =>
        handle(event)
        context.system.eventStream.publish(event) // default event-bus publishing for pub/sub
      }

    case Report =>
      log.info(s"Current count: $count")
      sender ! Result(count)

    case _ => log.error("Received unknown command")
  }

  // Restarting from events replay
  override def receiveRecover: Receive = {
    case event: CountIncrementRequested => handle(event)
    case _ => // ignore everything else
  }

  // Event Handler
  private def handle(event: CountIncrementRequested) {
    if (count == event.oldCount) count += 1
  }
}
