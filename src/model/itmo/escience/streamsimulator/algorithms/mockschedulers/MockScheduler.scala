package itmo.escience.streamsimulator.algorithms.mockschedulers

import itmo.escience.streamsimulator.entities.{Characteristics, Container}
import itmo.escience.streamsimulator.general.{Context, Environment, OperatorInstanceBasedSchedule}
import itmo.escience.streamsimulator.scheduler.Scheduler
import itmo.escience.streamsimulator.statistics.OperatorInstanceStats

import scala.collection.mutable
import scala.util.Random

/**
  * Created by mikhail on 30.01.2017.
  */
class MockScheduler extends Scheduler[OperatorInstanceBasedSchedule] {

  override def schedule(ctx: Context[OperatorInstanceBasedSchedule]): List[Context[OperatorInstanceBasedSchedule]] = {
    val rnd = new Random()
    val newCtx = ctx.clone()

    val newResources = ctx.env.allResources.filter(_.parentId == null).map{ res =>
      res.resources = List()
      res
    }

    newCtx.env = new Environment(newResources, newCtx.env.allNetworks)
    val env = newCtx.env
    val wld = newCtx.workload
    // allocation of containers
    for (res <- env.allResources) {
      val char = res.characteristics.clone()
      val contId = env.allocateContainer(res.id, char)
      val slotsNumber = 2//rnd.nextInt(2) + 1
      for (sIdx <- 0 until slotsNumber) {
        val slotChar = new Characteristics()
        slotChar.cpu = char.cpu / slotsNumber
        val slotId = env.allocateSlot(contId, slotChar)
      }
    }
    // division of operators
    val operators = wld.getOperators()
    for (op <- operators) {
      val tasksNumber = rnd.nextInt(3) + 1
      for (i <- 0 until tasksNumber) {
        val tId = wld.allocateTask(op.id)
      }
    }
    // schedule
    val taskIds = wld.getTaskIds()
    val slotIds = env.getSlotIds()
    val schedule = new OperatorInstanceBasedSchedule()
    for (tId <- taskIds) {
      val sIdx = rnd.nextInt(slotIds.size)
      schedule.operatorInstanceIdToSlotId.put(tId, slotIds(sIdx))
    }
    newCtx.schedule = schedule
    List[Context[OperatorInstanceBasedSchedule]](newCtx)
  }
}
