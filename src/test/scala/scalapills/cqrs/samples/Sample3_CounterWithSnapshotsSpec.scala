package scalapills.cqrs.samples

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scalapills.cqrs.domain._
import scalapills.cqrs.samples.CounterWithSnapshots.SnapshotInterval

class Sample3_CounterWithSnapshotsSpec extends TestKit(ActorSystem("CounterWithSnapshotsSpec")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  private val justBeforeSnapshotTrigger = SnapshotInterval - 1

  override protected def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An event-sourcing counter with snapshot actor" should {

    "not save any snapshot before the threshold of messages is reached" in {
      val counter = system.actorOf(Props(new CounterWithSnapshots("003")))

      an[AssertionError] shouldBe thrownBy {
        EventFilter.info(pattern = "Snapshot saved: \\d+", occurrences = 1) intercept {
          (1 to justBeforeSnapshotTrigger).foreach(_ => counter ! Increment)
        }
      }
    }

    "save snapshot when pre-defined number of messages is reached, and be restored from snapshot when restarted" in {
      val counter = system.actorOf(Props(new CounterWithSnapshots("004")))

      (1 to justBeforeSnapshotTrigger).foreach(_ => counter ! Increment)
      EventFilter.info(message = s"Snapshot saved: $SnapshotInterval", occurrences = 1) intercept {
        counter ! Increment
      }

      counter ! PoisonPill // kills the actor

      EventFilter.info(message = s"Restored from snapshot: $SnapshotInterval", occurrences = 1) intercept {
        system.actorOf(Props(new CounterWithSnapshots("004")))
      }
    }
  }
}
