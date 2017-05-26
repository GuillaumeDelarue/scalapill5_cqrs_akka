package scalapills.cqrs.samples

import akka.actor.ActorLogging
import akka.persistence.PersistentActor

import scalapills.cqrs.domain._

case class State(count: Int = 0,
                 anotherField: String = "initial value") {

  def updated(event: CountIncrementRequested): State =
    if (count == event.oldCount) copy(count = count + 1) else this

  def updated(event: AnotherEvent): State = copy(anotherField = event.newValue)
}

class CounterWithState(override val persistenceId: String) extends PersistentActor with ActorLogging {
  private var state = State()

  // Command handler
  override def receiveCommand: Receive = {
    case Increment =>
      persist(CountIncrementRequested(state.count)) { event =>
        handle(event)
        context.system.eventStream.publish(event)
      }

    case AnotherCommand(value) =>
      persist(AnotherEvent(state.anotherField, value)) { event =>
        handle(event)
        context.system.eventStream.publish(event)
      }

    case Report =>
      log.info(s"Current state: [$state]")
      sender ! Result(state.count)

    case _ => log.error("Received unknown command")
  }

  // Restarting from events replay
  override def receiveRecover: Receive = {
    case event: CountIncrementRequested => handle(event)
    case event: AnotherEvent => handle(event)
    case _ => // ignore everything else
  }

  // Event Handler
  private def handle(event: DomainEvent) {
    state = event match {
      case countIncrementRequested: CountIncrementRequested => state.updated(countIncrementRequested)
      case event: AnotherEvent => state.updated(event)
      case _ => state
    }
  }
}
