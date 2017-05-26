package scalapills.cqrs.samples

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scalapills.cqrs.domain.{CountIncrementRequested, Increment, Report, Result}

class Sample2_EventSourcingCounterSpec extends TestKit(ActorSystem("EventSourcingActorSpec")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  override protected def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An event-sourcing counter actor" should {

    "increment its value and report result when asked" in {
      val counter = system.actorOf(Props(new EventSourcingCounter("001"))) // not recommended! Could break encapsulation when closing over enclosing class

      assertResultOfReportCommandIs(counter, 0)
      counter ! Increment
      assertResultOfReportCommandIs(counter, 1)
    }

    "persist its state and be reload it after a restart" in {
      val persistenceId = "002"
      val counter = system.actorOf(Props(new EventSourcingCounter(persistenceId)))
      counter ! Increment
      assertResultOfReportCommandIs(counter, 1)

      counter ! PoisonPill // kills the actor

      val resurrectedCounter = system.actorOf(Props(new EventSourcingCounter(persistenceId)))
      assertResultOfReportCommandIs(resurrectedCounter, 1)
    }

    "publish event after incrementation" in {
      system.eventStream.subscribe(testActor, classOf[CountIncrementRequested])
      val counter = system.actorOf(Props(new EventSourcingCounter("003")))

      counter ! Increment
      counter ! Increment

      expectMsg(CountIncrementRequested(oldCount = 0))
      expectMsg(CountIncrementRequested(oldCount = 1))
    }

    "log all unknown commands received" in {
      val counter = system.actorOf(Props(new EventSourcingCounter("004")))
      EventFilter.error(message = s"Received unknown command", occurrences = 1) intercept {
        counter ! "Bunch of Monkeys"
      }
    }
  }

  private def assertResultOfReportCommandIs(actor: ActorRef, count: Int) = {
    EventFilter.info(message = s"Current count: $count", occurrences = 1) intercept {
      actor ! Report
    }
    expectMsg(Result(count)) // Assert a specific message has been received by the "testActor" (which is the implicit sender)
  }
}
