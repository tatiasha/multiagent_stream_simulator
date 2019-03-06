package model.general

import model.{ClusterCpusShare, OperatorId}

/**
  * Created by nikolay on 29.03.17.
  */
case class OperatorsShareSchedule(shares: Map[OperatorId, ClusterCpusShare]) extends Schedule
