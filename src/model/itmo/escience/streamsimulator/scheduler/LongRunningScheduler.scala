package itmo.escience.streamsimulator.scheduler

import itmo.escience.streamsimulator.general.{Context, OperatorInstanceBasedSchedule, Schedule}

/**
  * Created by nikolay on 27.03.17.
  */
trait LongRunningScheduler[TSchedule <: Schedule] extends Scheduler[TSchedule]{
  def updateStats(stats: AppStats): Unit
  def updateStats(stats: AppResourcesStats): Unit
  def updateContext(ctx: Context[TSchedule]): Unit
  def start(ctx: Context[TSchedule], callback: LongRunningSchedulerCallback[TSchedule]): Unit
  def stop(): Unit
  def status: LongRunningSchedulerStatus
}

trait LongRunningSchedulerCallback[TSchedule <: Schedule] {
  def updateSchedule(schedule: TSchedule): Unit
}

sealed trait LongRunningSchedulerStatus

case object Stopped extends LongRunningSchedulerStatus
case object Running extends LongRunningSchedulerStatus
