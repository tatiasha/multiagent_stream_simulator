//package itmo.escience.streamsimulator.experiments.baseintegration
//
//import itmo.escience.streamsimulator.algorithms.ga.GAScheduler
//import itmo.escience.streamsimulator.entities._
//import itmo.escience.streamsimulator.experiments.Experiment
//import itmo.escience.streamsimulator.general._
//import itmo.escience.streamsimulator.statistics.{OperatorInstanceStats, Statistics}
//
//import scala.collection.mutable
//import scala.concurrent.duration.Duration
//
///**
//  * Created by mikhail on 27.02.2017.
//  */
//class GABaseIntegrationInputExampleExp extends Experiment {
//
//  def run() {
//
//    val wld = getWorkload
//    val env = getEnvironment
//    val tenants = getTenants
//    val framework = new Framework("Spark", "6.6.6")
//    val schedule = new OperatorInstanceBasedSchedule()
//    val time = 0.0
//    val ctx = new Context(tenants, env, wld, schedule, time, framework)
//
//    val scheduler = new GAScheduler()
//
//    val instancesStart = ctx.workload.getTasks()
//    for (inst <- instancesStart) {
//      val op = ctx.workload.operatorMap.get(inst.operatorId)
//      var inputDataStart = List[(Long, Long)]()
//      var outputDataStart = List[(Long, Long)]()
//      for (i <- 0 until 100) {
//        inputDataStart :+= (i.toLong, (Math.sin(i / 5.0 * Math.PI) * 100 * 0.2 + 100).toLong)
//        outputDataStart :+= (i.toLong, (Math.sin(i / 5.0 * Math.PI) * 90 * 0.2 + 90).toLong)
//      }
//      val instStat = new OperatorInstanceStats(inst.id, inputDataStart, outputDataStart, List[(Long, Double)](), Duration(1, "millis"))
////      scheduler.addStats(mutable.MutableList[OperatorInstanceStats](instStat))
//    }
//
//
//
//    val ctx1 = scheduler.schedule(ctx).head
//    println("First phase completed")
//    val ctx2 = ctx1.clone()
//    val instances = ctx2.workload.getTasks()
//    for (inst <- instances) {
//      val op = ctx2.workload.operatorMap.get(inst.operatorId)
//      var inputData = List[(Long, Long)]()
//      var outputData = List[(Long, Long)]()
//      for (i <- 0 until 1000) {
//        inputData :+= (i.toLong, (Math.sin(i / 5.0 * Math.PI) * 100 * 0.2 + 100).toLong)
//        outputData :+= (i.toLong, (Math.sin(i / 5.0 * Math.PI) * 90 * 0.2 + 90).toLong)
//      }
//      val instStat = new OperatorInstanceStats(inst.id, inputData, outputData, List[(Long, Double)](), Duration(1, "millis"))
//      scheduler.addStats(mutable.MutableList[OperatorInstanceStats](instStat))
//    }
//    println("Start second")
//    val ctx3 = scheduler.schedule(ctx2).head
//    println("Phase 2 completed")
//  }
//
//
//  def getWorkload = {
//    var inst11 = new OperatorInstance("t1_a1_o1_i1", "t1_a1_o1", List[String](), List[String]())
//    var oper11 = new Operator("t1_a1_o1", "t1_a1", List[String](), List[String](), List[OperatorInstance](inst11), List[String]("sensors=200"), "BFS")
//    var app11 = new App("t1_a1", "t1", List[Operator](oper11))
//    var inst12 = new OperatorInstance("t1_a2_o1_i1", "t1_a2_o1", List[String](), List[String]())
//    var oper12 = new Operator("t1_a2_o1", "t1_a2", List[String](), List[String](), List[OperatorInstance](inst12), List[String]("sensors=100"), "BFS")
//    var app12 = new App("t1_a2", "t1", List[Operator](oper12))
//    val tenant1 = new Tenant("t1", 1)
//    tenant1.apps = List[AppId]("t1_a1", "t1_a2")
//    var inst21 = new OperatorInstance("t2_a1_o1_i1", "t2_a1_o1", List[String](), List[String]())
//    var oper21 = new Operator("t2_a1_o1", "t2_a1", List[String](), List[String](), List[OperatorInstance](inst21), List[String]("sensors=300"), "BFS")
//    var app21 = new App("t2_a1", "t2", List[Operator](oper21))
//
//    new Workload(List[App](app11, app12, app21))
//  }
//
//  def getTenants = {
//    val tenant1 = new Tenant("t1", 1)
//    tenant1.apps = List[AppId]("t1_a1", "t1_a2")
//    val tenant2 = new Tenant("t2", 1)
//    tenant2.apps = List[AppId]("t2_a1")
//
//    List[Tenant](tenant1, tenant2)
//  }
//
//  def getEnvironment = {
//    val res1char = new Characteristics()
//    res1char.cpu=800.0
//    res1char.ram=1600.0
//    val res1 = new Resource("res1", "glob", res1char)
//    val net = new Network("glob", 100*1024, List("res1"))
//    val env = new Environment(List(res1), List(net))
//    val cont1char = res1char.clone()
//    env.allocateContainer("res1", cont1char)
//    env
//  }
//
//}
