package itmo.escience.streamsimulator.general

import itmo.escience.streamsimulator.{ClusterCpusShare, OperatorId}

/**
  * Created by nikolay on 29.03.17.
  */
case class OperatorsShareSchedule(shares: Map[OperatorId, ClusterCpusShare]) extends Schedule
