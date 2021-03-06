package scalapills.cqrs.samples

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SnapshotOffer}

import scalapills.cqrs.domain._
import scalapills.cqrs.samples.CounterWithSnapshots.SnapshotInterval

object CounterWithSnapshots {
  val SnapshotInterval = 100
}

class CounterWithSnapshots(override val persistenceId: String) extends PersistentActor with ActorLogging {
  private var count = 0

  // Command handler
  override def receiveCommand: Receive = {
    case Increment =>
      persist(CountIncrementRequested(count)) { event =>
        handle(event)
        context.system.eventStream.publish(event)
        if (lastSequenceNr % SnapshotInterval == 0 && lastSequenceNr != 0) {
          saveSnapshot(count)
          log.info(s"Snapshot saved: $count")
        }
      }

    case Report =>
      log.info(s"Current count: $count")
      sender ! Result(count)

    case c => log.error(s"Received unknown command $c")
  }

  // Restarting from events replay
  override def receiveRecover: Receive = {
    case event: CountIncrementRequested => handle(event)
    case SnapshotOffer(_, snapshot: Int) =>
      count = snapshot
      log.info(s"Restored from snapshot: $count")
    case _ => // ignore everything else
  }

  // Event Handler
  private def handle(event: CountIncrementRequested) {
    if (count == event.oldCount) count += 1
  }
}
