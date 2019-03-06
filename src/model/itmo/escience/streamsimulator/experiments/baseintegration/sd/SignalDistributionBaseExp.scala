package itmo.escience.streamsimulator.experiments.baseintegration.sd

import itmo.escience.streamsimulator.algorithms.signal_distr_ga.{SDSchedulerImproved, SDEngine}
import itmo.escience.streamsimulator.scheduler.LongRunningSchedulerCallback
import itmo.escience.streamsimulator.{ResourceId, OperatorId}
import itmo.escience.streamsimulator.entities._
import itmo.escience.streamsimulator.experiments.Experiment
import itmo.escience.streamsimulator.general._

/**
  * Created by mikhail on 29.03.2017.
  */
class SignalDistributionBaseExp extends Experiment {
  override def run(): Unit = {
    var tenants: List[Tenant] = List[Tenant]()
    val tenantsSize = 10
    val resourcesSize = 15
    val resCores = 32
    val containersLimit = 4
    val partitionLimit = 3

    var apps: List[App] = List[App]()
    for (tenantIdx <- 0 until tenantsSize) {
      val tenantId = s"T$tenantIdx"
      val appId = s"${tenantId}A0"
      val opId = s"${appId}O0"
      val operator = new Operator(opId, appId, List[OperatorId](), List[OperatorId](), List[OperatorInstance](), partitionLimit)
      val tenantApp = new App(appId, tenantId, List[Operator](operator))
      val tenant = new Tenant(tenantId)
      tenants :+= tenant
      apps :+= tenantApp
    }
    val workload = new Workload(apps)

    var resources = List[Resource]()
    for (resIdx <- 0 until resourcesSize) {
      val resId = s"N$resIdx"
      val res = new Node(resId, resCores, containersLimit)
      resources :+= res
    }
    val env = new Environment(resources, null)

    val ctx = new Context[NodeOperatorContainerBasedScheduleImproved](tenants, env, workload, null, 0.0, null)

    val alg = new SDSchedulerImproved()
    println(alg.status)
    alg.start(ctx, new LongRunningSchedulerCallback[NodeOperatorContainerBasedScheduleImproved] {
      override def updateSchedule(schedule: NodeOperatorContainerBasedScheduleImproved): Unit = {
        println("Ok")
      }
    })
  }
}
