package timeTracker

import java.time.Instant

import akka.pattern.pipe
import akka.persistence.PersistentActor
import timeTracker.StopwatchActor.{GetEvents, GetEventsResponse, StartStopEvent, StopwatchActorState}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class StopwatchActor extends PersistentActor {

  private var state = StopwatchActorState(Nil)

  private def updateState(event: StartStopEvent): Unit = {
    state = state.copy(events = state.events :+ event)
  }

  override def receiveRecover: Receive = {
    case e: StartStopEvent =>
      println("receiveRecover")
      updateState(e)
  }

  override def receiveCommand: Receive = {
    case e: StartStopEvent => persist(e)(updateState)
    case _: GetEvents => pipe(Future(GetEventsResponse(state.events))).to(sender())
    //      println("printing state")
    //      state.events.foreach(e =>
    //        println(s"${e.getClass.getSimpleName} ${e.time}")
    //      )
  }

  override def persistenceId: String = "StopwatchActor-1"
}

object StopwatchActor {

  trait StartStopEvent {
    val time: Instant
  }

  case class StopEvent(time: Instant = Instant.now()) extends StartStopEvent

  case class StartEvent(time: Instant = Instant.now()) extends StartStopEvent

  case class GetEvents()

  case class GetEventsResponse(events: Seq[StartStopEvent])

  case class StopwatchActorState(events: Seq[StartStopEvent])

}