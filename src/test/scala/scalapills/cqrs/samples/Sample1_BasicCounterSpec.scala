package scalapills.cqrs.samples

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

import scalapills.cqrs.domain.{Increment, Report, Result}

class Sample1_BasicCounterSpec extends TestKit(ActorSystem("BasicActorSpec")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  override protected def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "A counter actor" should {

    "increment its value and report result when asked" in {
      val counter = system.actorOf(Props[BasicCounter])
      assertResultOfReportCommandIs(counter, 0)

      counter ! Increment
      assertResultOfReportCommandIs(counter, 1)
    }

    "log all unknown messages received" in {
      val counter = system.actorOf(Props[BasicCounter])
      val invalidMessage = "Bunch of Monkeys"

      EventFilter.error(message = s"Received unknown message: [$invalidMessage]", occurrences = 1) intercept {
        counter ! invalidMessage
      }
    }

    "performance testing" in {
      val counter = system.actorOf(Props[BasicCounter])

      within(100 millis) {
        counter ! Increment

        counter ! Report
        expectMsg(Result(1))
        expectNoMsg() // blocks for the rest of the 100 millis
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
