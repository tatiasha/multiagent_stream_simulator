package model.utilities

import model.entities.{Container, Node}
import model.general.NodeOperatorContainerBasedScheduleImproved.CREATABLE_CONTAINER_PREFIX
import model.general.{Context, NodeOperatorContainerBasedScheduleImproved}
import model.{AppId, ContainerId}

import scala.collection.JavaConversions._

/**
  * Created by nikolay on 01.04.17.
  */
object SchedulerValidationUtils {

  def validate(schedule: NodeOperatorContainerBasedScheduleImproved,
               oldCtx: Context[NodeOperatorContainerBasedScheduleImproved]): Unit = {
    containersShouldBeUnique(schedule, oldCtx)
    containersShouldBelongToExistingResources(schedule, oldCtx)
    containersShouldBeExistingOrCreatable(schedule, oldCtx)
  }

  private def containersShouldBeUnique(schedule: NodeOperatorContainerBasedScheduleImproved,
                                       oldCtx: Context[NodeOperatorContainerBasedScheduleImproved]) = {
    val containersIds = schedule.nodesMapping
      .flatMap {case (resId, containers) => containers.map(_._2)}
      .filter(_ != CREATABLE_CONTAINER_PREFIX)
      .toList

    val uniqueContainersIds = containersIds.toSet
    if (containersIds.size != uniqueContainersIds.size) {
      throw new ScheduleValidationException(s"Schedule contains duplicated container ids: " +
        s"count of all ids ${containersIds.size} vs count of unique ids ${uniqueContainersIds.size}")
    }
  }

  private def containersShouldBelongToExistingResources(schedule: NodeOperatorContainerBasedScheduleImproved,
                                                oldCtx: Context[NodeOperatorContainerBasedScheduleImproved]): Unit = {
    val resources = oldCtx.env.resources.filter(!_.isInstanceOf[Container]).map(_.id).toSet
    val allResourcesAreExisting = schedule.nodesMapping.keySet().subsetOf(resources)

    if (!allResourcesAreExisting) {
      throw new ScheduleValidationException("Schedule maps containers on not " +
        "existing resources or some resources in schedule actually containers in current context")
    }
  }

  private def containersShouldBeExistingOrCreatable(schedule: NodeOperatorContainerBasedScheduleImproved,
                                            oldCtx: Context[NodeOperatorContainerBasedScheduleImproved]): Unit = {
    for( (resId, containers) <- schedule.nodesMapping) {
      val res = oldCtx.env.resource(resId).get
      val node = res.asInstanceOf[Node]
      for(tpl @ (appId, contId, dedicatedCores, sharedCores) <- containers) {
        appExists(appId, oldCtx)
        containerIsExistingOrCreatable(node, tpl, oldCtx)
      }

      dedicatedPartsDoesNotExceedResourceCapabilities(node, containers, oldCtx)
    }
  }

  private def appExists(appId: AppId, oldCtx: Context[NodeOperatorContainerBasedScheduleImproved]): Unit = {
    if (!oldCtx.workload.appMap.containsKey(appId)) {
      throw new ScheduleValidationException(s"Schedule contains app $appId which doesn't exist")
    }
  }

  private def containerIsExistingOrCreatable(node: Node,
                                     tpl: (AppId, ContainerId, Int, Int),
                                     oldCtx: Context[NodeOperatorContainerBasedScheduleImproved]): Unit = {

    val (appId, contId, dedCpu, sharedCpu) = tpl

    if (dedCpu < 0 || sharedCpu < 0) {
      throw new ScheduleValidationException("Count of allocating cpu can not be negative")
    }

    if (dedCpu + sharedCpu == 0){
      throw new ScheduleValidationException(
        "Count of allocating cpu cannot be zero simultaneously for dedicated and shared"
      )
    }

    val isExisting = oldCtx.env.getContIds().contains(contId)
    val cont = if (isExisting) oldCtx.env.getContainers().find(_.id == contId).get else null
    // container must have the same characteristcis as the existing one
    val isConformedToExisting = if (isExisting) {
      cont.characteristics.cpu / 100 == dedCpu && cont.characteristics.sharedCpu / 100 == sharedCpu
    } else {
      false
    }

    if (isExisting && !isConformedToExisting) {
      throw new ScheduleValidationException(
        s"Schedule points to existing container $contId but with different in size: " +
          s"existing (ded: ${cont.characteristics.cpu / 100}, shared: ${cont.characteristics.sharedCpu / 100}) " +
          s"vs from schedule (ded: $dedCpu, shared: $sharedCpu)"
      )
    }

//    val creatable =  contId.startsWith(CREATABLE_CONTAINER_PREFIX)
    val creatable = isCreatable(contId, oldCtx)
    if (!isExisting && !creatable) {
      throw new ScheduleValidationException(s"Schedule contains container $contId  " +
        s"which is not existing " + s"and doesn't have id starting with '---' ")
    }

    val creatableExceedsTotatCpuCount = node.cpuCount < dedCpu + sharedCpu

    if (!isExisting && creatableExceedsTotatCpuCount) {
      throw new ScheduleValidationException(s"Schedule contains container $contId  " +
        s"which is not existing " + s"and its characteristics exceeds ones of resource: " +
        s"existing ${node.cpuCount}" +
        s"vs from schedule (ded: $dedCpu, shared: $sharedCpu)")
    }
  }

  private def dedicatedPartsDoesNotExceedResourceCapabilities(node: Node,
                                                      containers: List[(AppId, ContainerId, Int, Int)],
                                                      oldCtx: Context[NodeOperatorContainerBasedScheduleImproved]): Unit = {
    val totalDedicatedCpuCount = containers.map {case (_,_,dedCpu,_) => dedCpu}.sum
    val totalCpuCountOnNode = node.cpuCount

    if (totalDedicatedCpuCount > totalCpuCountOnNode) {
      throw new ScheduleValidationException(
        s"Count of total dedicated cpu for containers on this node are greater than total cpu count: " +
          s"$totalDedicatedCpuCount > $totalCpuCountOnNode"
      )
    }
  }

  def isCreatable(contId: ContainerId,  ctx: Context[NodeOperatorContainerBasedScheduleImproved]): Boolean = {
    contId.startsWith(CREATABLE_CONTAINER_PREFIX) || ctx.env.container(contId).isEmpty
  }

}

class ScheduleValidationException(msg: String) extends RuntimeException(msg)
