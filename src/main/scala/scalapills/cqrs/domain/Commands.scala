package scalapills.cqrs.domain

sealed class Command

case object Increment extends Command

case object Report extends Command

case class Result(count: Int) extends Command

case class AnotherCommand(value: String) extends Command