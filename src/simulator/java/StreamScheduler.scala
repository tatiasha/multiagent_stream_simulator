import java.util

import itmo.escience.streamsimulator._
import itmo.escience.streamsimulator.algorithms.signal_distr_ga.{SDGene, SDSolution, SDEngine, SDSchedulerImproved}
import itmo.escience.streamsimulator.entities._
import itmo.escience.streamsimulator.general.{Workload, Environment, Context, NodeOperatorContainerBasedScheduleImproved}
import itmo.escience.streamsimulator.scheduler.{OperatorStats, AppStats}
import scala.collection.JavaConversions._

/**
  * Created by mikhail on 28.04.2017.
  */
object StreamScheduler {

  def schedule(state: StreamState) = {
    // transform simulator's objects to model
    val simWorkloadGrid = state.workload
    val simNodesGrid = state.nodes
    val simWorkload = state.workloadMap
    val simNodes = state.nodesMap

    // environment
    val schedule = new NodeOperatorContainerBasedScheduleImproved()
    val opStatsMap = new util.HashMap[OperatorId, List[OperatorStats]]()

    var resources = List[Resource]()
    for (simNode <- simNodes) {
      val res = new Node(simNode._1, simNode._2.cores, simNodesGrid.getHeight - 1)
      resources :+= res
      schedule.nodesMapping.put(res.id, List[(AppId, ContainerId, Int, Int)]())
    }
    //workload
    var tenants: List[Tenant] = List[Tenant]()
    var apps: List[App] = List[App]()
    for (simOp <- simWorkload) {
      val opAgent = simOp._2
      val tenantId = opAgent.id
      val appId = tenantId
      val opId = appId
      val operator = new Operator(opId, appId, List[OperatorId](), List[OperatorId](), List[OperatorInstance](), 1)
      val tenantApp = new App(appId, tenantId, List[Operator](operator))
      val tenant = new Tenant(tenantId)
      tenants :+= tenant
      apps :+= tenantApp
      // sched
      if (simNodesGrid.exists(opAgent) && opAgent.isExecuting) {
        val opLocation = simNodesGrid.getObjectLocation(opAgent)
        val objectsAtLoc = simNodesGrid.getObjectsAtLocation(opLocation.x, 0)
        val nodeAtLoc = objectsAtLoc.head.asInstanceOf[NodeAgent]
        schedule.nodesMapping.put(nodeAtLoc.id, schedule.nodesMapping.get(nodeAtLoc.id) :+(appId, nodeAtLoc.id + "_" + appId, 0, 1))
      }
      // stats
      var opStats = List[OperatorStats]()
      for (stat <- opAgent.inputForecast) {
        opStats :+= new OperatorStats(stat(0).toInt, stat(1).toLong, 0, 0, 0)
      }
      opStatsMap.put(opId, opStats)
    }

    val env = new Environment(resources, null)
    val workload = new Workload(apps)
    val ctx = new Context[NodeOperatorContainerBasedScheduleImproved](tenants, env, workload, schedule, state.schedule.getTime, null)
    val stats = new AppStats(opStatsMap.toMap, state.schedule.getTime.toInt, state.schedule.getTime.toInt + 20, 1)

    val alg: SDEngine[NodeOperatorContainerBasedScheduleImproved] = new SDEngine[NodeOperatorContainerBasedScheduleImproved]()
    alg.build(ctx, stats)
    val solution = alg.run(50, 100)
    solToJava(solution)
  }

  def solToJava(sol: SDSolution) = {
    val chroms = new util.HashMap[String, util.ArrayList[SDGene]]()
    for (chrom <- sol.chromosomes) {
      val genes = new util.ArrayList[SDGene]()
      for (gene <- chrom.genes) {
        genes.add(gene)
      }
      chroms.put(chrom.nodeId, genes)
    }
    chroms
  }
}
