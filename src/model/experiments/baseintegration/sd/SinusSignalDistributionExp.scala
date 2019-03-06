package model.experiments.baseintegration.sd

import java.io.{PrintWriter, File}
import java.util.Random

import model.algorithms.signal_distr_ga.heuristic.SDHeuristicAlgorithm
import model.utilities.{ExpProps, Misc}
import model._
import model.algorithms.signal_distr_ga._
import model.entities._
import model.experiments.Experiment
import model.general._
import model.scheduler.{OperatorStats, AppStats, LongRunningSchedulerCallback}
import java.util
import scala.collection.JavaConversions._

import scala.io.Source

/**
  * Created by mikhail on 29.03.2017.
  */
class SinusSignalDistributionExp extends Experiment {
  override def run(): Unit = {
    val files_directory = "C:\\Users\\Local\\Desktop\\Siemens_05_05_17\\Projects\\StreamSimLaunch"
    ExpProps.setPropsFromFile(files_directory)
    var tenants: List[Tenant] = List[Tenant]()
    val tenantsSize = ExpProps.tenantsNumber
    val resourcesSize = ExpProps.nodesNumber
    val resCores = ExpProps.cores
    val containersLimit = 4
    val partitionLimit = 1

    val randomInitSchedule = new util.HashMap[ResourceId, List[(AppId, ContainerId, Int, Int)]]()

    var apps: List[App] = List[App]()
    for (tenantIdx <- 0 until tenantsSize) {
      val tenantId = s"${tenantIdx + 1}"
      val appId = tenantId
      val opId = appId
      val operator = new Operator(opId, appId, List[OperatorId](), List[OperatorId](), List[OperatorInstance](), partitionLimit)
      val tenantApp = new App(appId, tenantId, List[Operator](operator))
      val tenant = new Tenant(tenantId)
      tenants :+= tenant
      apps :+= tenantApp
    }
    val workload = new Workload(apps)

    var resources = List[Resource]()
    for (resIdx <- 0 until resourcesSize) {
      val resId = s"${resIdx + 1}"
      val res = new Node(resId, resCores, containersLimit)
      resources :+= res
      randomInitSchedule.put(res.id, List[(AppId, ContainerId, Int, Int)]())
    }
    val random = new Random()
    for (app <- apps) {
      val chosenRes = Misc.getRandomElement(resources, random)
      randomInitSchedule.put(chosenRes.id, randomInitSchedule.get(chosenRes.id) :+(app.id, chosenRes.id + "_" + app.id, 0, 1))
    }
    val initSchedule = new NodeOperatorContainerBasedScheduleImproved()
    initSchedule.nodesMapping = randomInitSchedule

    val env = new Environment(resources, null)

    val ctx = new Context[NodeOperatorContainerBasedScheduleImproved](tenants, env, workload, initSchedule, 0.0, null)

    val alg = new SDSchedulerImproved()
    val (appSinusStats, initialSchedule) = getAppStatFromFiles(files_directory + "/signals") // TODO path with input signals

    alg.updateStats(appSinusStats)
    println(alg.status)
    alg.start(ctx, (schedule: NodeOperatorContainerBasedScheduleImproved) => {
      println("Ok")
    })
  }


  def getAppStatFromFiles(path: String): (AppStats, Map[ResourceId, List[OperatorId]]) = {
    val opStatsMap = new util.HashMap[OperatorId, List[OperatorStats]]()
    val schedule = new util.HashMap[ResourceId, List[OperatorId]]()
    val files = getListOfFiles(path)
    for (filename <- files) {
      println(filename)
      val parsedString = filename.getAbsolutePath.split("_").takeRight(2)
      val tenantId = Integer.parseInt(parsedString(0))
      val nodeId = Integer.parseInt(parsedString(1).replace(".out", ""))
      if (!schedule.containsKey(nodeId + "")) {
        schedule.put(nodeId + "", List[OperatorId]())
      }
      schedule.put(nodeId + "", schedule.get(nodeId + "") :+ (tenantId + ""))
      var valuesList = List[OperatorStats]()
      var idx = 0
      for (line <- Source.fromFile(filename).getLines()) {
        val value = line.replace(",", ".").toDouble
        valuesList :+= new OperatorStats(idx, value.toLong, 0, 0, 0)
        idx += 1
      }
      opStatsMap.put(tenantId + "", valuesList)
    }
    (new AppStats(opStatsMap.toMap, 0, 180, 1), schedule.toMap)
  }

  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def mapToSDSolution(schedule: Map[ResourceId, List[OperatorId]]): SDSolution = {
    var chromosomes = List[SDChromosome]()
    for (key <- schedule.keys) {
      var genes = List[SDGene]()
      for (opId <- schedule(key)) {
        genes :+= new SDGene(opId, 0, 1)
      }
      chromosomes :+= new SDChromosome(key, genes)
    }
    new SDSolution(chromosomes)
  }

  def solutinToData(sol: SDSolution, out: String) = {
    val filePrinter = new PrintWriter(out)
    for (chrom <- sol.chromosomes) {
      filePrinter.write(chrom.nodeId)
      for (gene <- chrom.genes) {
        filePrinter.write("\t" + gene.operatorId)
      }
      filePrinter.write("\n")
    }
    filePrinter.close()
  }


}
