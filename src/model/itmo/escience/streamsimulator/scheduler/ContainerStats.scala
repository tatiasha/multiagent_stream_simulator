package itmo.escience.streamsimulator.scheduler

import itmo.escience.streamsimulator._

/**
  * Created by nikolay on 27.03.17.
  */

case class AppResourcesStats(contMap: Map[ResourceId, List[ContainerStats]],
                             begin: Timestamp,
                             end: Timestamp,
                             delta: Long)

case class ContainerStats(contId: ResourceId, stats: List[OpExecStats])

case class OpExecStats(timestamp: Timestamp,
                       batchNum : BatchNum,
                       batchDataSize: AverageBatchSize)
