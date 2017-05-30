package scalapills.cqrs.domain

sealed class DomainEvent

case class CountIncrementRequested(oldCount: Int) extends DomainEvent

case class AnotherEvent(oldValue: String, newValue: String) extends DomainEvent

case class SnapshotSaved(persistenceId: String, count: Int) extends DomainEvent