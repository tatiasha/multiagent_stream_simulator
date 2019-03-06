package itmo.escience.streamsimulator.algorithms.signal_distr_ga

import itmo.escience.streamsimulator.general.{Context, NodeOperatorContainerBasedSchedule, NodeOperatorContainerBasedScheduleImproved}
import itmo.escience.streamsimulator.scheduler._
import itmo.escience.streamsimulator.utilities.{ExpProps, SchedulerValidationUtils}

/**
  * Created by mikhail on 29.03.2017.
  */
class SDSchedulerImproved extends LongRunningScheduler[NodeOperatorContainerBasedScheduleImproved] {

  var _stats: AppStats = null
  var _ctx: Context[NodeOperatorContainerBasedScheduleImproved] = null
  var _status: LongRunningSchedulerStatus = null
  var _callback: LongRunningSchedulerCallback[NodeOperatorContainerBasedScheduleImproved] = null
  var _schedules: List[Context[NodeOperatorContainerBasedScheduleImproved]] = null
  val _engine: SDEngine[NodeOperatorContainerBasedScheduleImproved] = new SDEngine[NodeOperatorContainerBasedScheduleImproved]()

  override def updateStats(stats: AppStats): Unit = {
    _stats = stats
  }

  override def updateContext(ctx: Context[NodeOperatorContainerBasedScheduleImproved]): Unit = {
    _ctx = ctx
  }

  override def stop(): Unit = {
    _status = Stopped
  }

  override def status: LongRunningSchedulerStatus = {
    _status
  }

  override def start(ctx: Context[NodeOperatorContainerBasedScheduleImproved],
                     callback: LongRunningSchedulerCallback[NodeOperatorContainerBasedScheduleImproved]): Unit = {

//    val props = SDSchedulerUtils.buildExpProps(ctx)
    val props = ExpProps

    _status = Running
    _engine.build(ctx, _stats)
    val resultSolution = _engine.run(props.popSize, props.iterations)
    val resultSchedule = SDProblem.solutionToScheduleImproved(resultSolution, ctx)
    val newCtx = new Context[NodeOperatorContainerBasedScheduleImproved](null, null, null, resultSchedule, 0.0, null)
    // TODO! context only with NEW SCHEDULE!!!
    _schedules = List[Context[NodeOperatorContainerBasedScheduleImproved]](newCtx)
    _status = Stopped
    callback.updateSchedule(resultSchedule)
  }

  override def updateStats(stats: AppResourcesStats): Unit = {

  }

  override def schedule(ctx: Context[NodeOperatorContainerBasedScheduleImproved]): List[Context[NodeOperatorContainerBasedScheduleImproved]] = {
    _schedules
  }
}
