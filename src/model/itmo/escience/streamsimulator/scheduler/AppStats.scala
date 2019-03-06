package itmo.escience.streamsimulator.scheduler

import itmo.escience.streamsimulator.{EndToEndlatency, InputThroughput, OutputThroughput, Timestamp, _}


/**
  * Created by nikolay on 27.03.17.
  */
case class AppStats(opStats: Map[OperatorId, List[OperatorStats]],
                    begin: Timestamp,
                    end: Timestamp,
                    delta: Long)

case class OperatorStats(timestamp: Timestamp,
                         inputThroughput: InputThroughput,
                         outputThroughput: OutputThroughput,
                         endToEndlatency: EndToEndlatency,
                         frameworkLatency: FrameworkLatency)

