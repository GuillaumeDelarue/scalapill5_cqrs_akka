package scalapills.cqrs.samples

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scalapills.cqrs.domain.{Increment, SnapshotSaved}

class Sample3_CounterWithSnapshotsSpec extends TestKit(ActorSystem("CounterWithSnapshotsSpec")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  override protected def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An event-sourcing counter with snapshot actor" should {

    "save snapshot every 100 messages received" in {
      val counter = system.actorOf(Props(new CounterWithSnapshots("001")))

      (1 to 99).foreach(_ => counter ! Increment)
      expectNoMsg()

      counter ! Increment

      expectMsg(SnapshotSaved("001", 100))
    }
  }
}
