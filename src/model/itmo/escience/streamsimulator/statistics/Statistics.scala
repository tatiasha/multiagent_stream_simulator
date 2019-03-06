//package itmo.escience.streamsimulator.statistics
//
//import itmo.escience.streamsimulator.algorithms.prediction.Predictor
//import itmo.escience.streamsimulator.entities.OperatorInstanceId
//import itmo.escience.streamsimulator.general.Context
//
//import scala.collection.JavaConversions._
//import java.util
//
///**
//  * Created by mikhail on 28.02.2017.
//  */
//class Statistics extends Cloneable with Serializable {
//
//  var statMap = new util.HashMap[OperatorInstanceId, OperatorInstanceStats]()
//  var instPredMap = new util.HashMap[OperatorInstanceId, OperatorInstanceStats]()
//  // String = operator type
//  var opAggregateStat = new util.HashMap[String, OperatorInstanceStats]()
//  var opAggregatePredStat = new util.HashMap[String, OperatorInstanceStats]()
//
//  // PerfModels
//
//
//  def inputPrediction(ctx: Context[_]) = {
//    // predict input for instances
//    val instances = ctx.workload.getTasks()
//    for (op <- instances) {
//      val opId = op.id
//      val opType = ctx.workload.operatorMap.get(op.operatorId).opType
//      val inStat = statMap.get(opId)
//      if (statMap.containsKey(opId)) {
//        val outStat = Predictor.signalPrediction(inStat)
//        instPredMap.put(opId, outStat)
//        if (!opAggregateStat.containsKey(opType)) {
//          opAggregateStat.put(opType, inStat.clone())
//        }
//        opAggregateStat.put(opType, opAggregateStat.get(opType).union(inStat.clone()))
//
//        if (!opAggregatePredStat.containsKey(opType)) {
//          opAggregatePredStat.put(opType, outStat.clone())
//        }
//        opAggregatePredStat.put(opType, opAggregatePredStat.get(opType).union(outStat.clone()))
//      }
//    }
//  }
//
//
//  override def clone(): Statistics = {
//    val newStat = new Statistics()
//    newStat.statMap = this.statMap
//    newStat
//  }
//}
