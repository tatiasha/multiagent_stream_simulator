package model.scheduler

import model.general.{Context, Environment, Schedule}

/**
  * Created by mikhail on 24.01.2017.
  */
trait Scheduler[TSchedule <: Schedule] {
  def schedule(ctx: Context[TSchedule]): List[Context[TSchedule]]
}