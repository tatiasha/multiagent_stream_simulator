package itmo.escience

/**
  * Created by nikolay on 03.02.17.
  */
package object streamsimulator {

  type AppId = String
  type UserId = String

  type NetworkId = String
  type ResourceId = String
  type ContainerId = String

  type OperatorId = String
  type SlotId = String

  type OperatorInstanceId = String
  type TenantId = String

  type Timestamp = Long
  type InputThroughput = Long
  type OutputThroughput = Long
  type EndToEndlatency = Double
  type FrameworkLatency = Double

  type BatchNum = Long
  type AverageBatchSize = Double

  type ClusterCpusShare = Double

}
