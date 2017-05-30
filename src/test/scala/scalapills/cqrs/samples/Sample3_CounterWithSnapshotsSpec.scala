package scalapills.cqrs.samples

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scalapills.cqrs.domain._

class Sample3_CounterWithSnapshotsSpec extends TestKit(ActorSystem("CounterWithSnapshotsSpec")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  override protected def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An event-sourcing counter with snapshot actor" should {

    "save snapshot every 100 messages received, and is restored from snapshot" in {
      val counter = system.actorOf(Props(new CounterWithSnapshots("003")))

      (1 to 99).foreach(_ => counter ! Increment)

      EventFilter.info(message = s"Snapshot saved: 100", occurrences = 1) intercept {
        counter ! Increment
      }

      counter ! PoisonPill // kills the actor

      EventFilter.info(message = s"Restored from snapshot: 100", occurrences = 1) intercept {
        system.actorOf(Props(new CounterWithSnapshots("003")))
      }
    }
  }
}
