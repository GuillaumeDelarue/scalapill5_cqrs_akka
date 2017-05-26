package scalapills.cqrs.samples

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SnapshotOffer}

import scalapills.cqrs.domain.{CountIncrementRequested, Increment, Report, Result}

class CounterWithSnapshots(override val persistenceId: String) extends PersistentActor with ActorLogging {
  private val snapShotInterval = 1000
  private var count = 0

  // Command handler
  override def receiveCommand: Receive = {
    case Increment =>
      persist(CountIncrementRequested(count)) { event =>
        handle(event)
        context.system.eventStream.publish(event)
        if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0) saveSnapshot(count)
      }

    case Report =>
      log.info(s"Current count: $count")
      sender ! Result(count)

    case _ => log.error("Received unknown command")
  }

  // Restarting from events replay
  override def receiveRecover: Receive = {
    case event: CountIncrementRequested => handle(event)
    case SnapshotOffer(_, snapshot: Int) => count = snapshot
    case _ => // ignore everything else
  }

  // Event Handler
  private def handle(event: CountIncrementRequested) {
    if (count == event.oldCount) count += 1
  }
}
