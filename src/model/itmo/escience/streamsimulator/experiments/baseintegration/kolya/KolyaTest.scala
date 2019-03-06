//package itmo.escience.streamsimulator.experiments.baseintegration.kolya
//
//import java.io.{FileReader, BufferedReader}
//import java.nio.file.{Path, Paths}
//
//import itmo.escience.streamsimulator.algorithms.signal_distr_ga.{SDProblem, SDEngine, SDSchedulerUtils}
//import itmo.escience.streamsimulator.experiments.Experiment
//import itmo.escience.streamsimulator.general.{Context, NodeOperatorContainerBasedScheduleImproved}
//import itmo.escience.streamsimulator.scheduler.{StatsUtils, AppStats}
//
//import scala.collection.JavaConverters._
//
///**
//  * Created by mikhail on 07.04.2017.
//  */
//object KolyaTest extends Experiment {
//  override def run(): Unit = {
//
//    import itmo.escience.streamsimulator.utilities.ContextJsonProtocol._
//    import spray.json._
//    import spray.json.DefaultJsonProtocol._
//
////    val pathCtx = Paths.get("D:\\Projects\\streamsimulator\\src\\main\\resources\\json_input_test\\context_0205.json")
//    val pathCtx = Paths.get("D:\\Projects\\streamsimulator\\src\\main\\resources\\json_input_test\\context_after_reconf_0405.json")
//    val jsonctx = readAllLines(pathCtx).last
//    val ctx = jsonctx
//      .parseJson
//      .convertTo[Context[NodeOperatorContainerBasedScheduleImproved]]
//
////    val pathAppStats = Paths.get("D:\\Projects\\streamsimulator\\src\\main\\resources\\json_input_test\\appstats_0205.json")
//    val pathAppStats = Paths.get("D:\\Projects\\streamsimulator\\src\\main\\resources\\json_input_test\\appstats_after_reconf_0405.json")
//    val jsonAppStats = readAllLines(pathAppStats).last
//
//    val props = SDSchedulerUtils.buildExpProps(ctx)
//    //TODO: fix it later
//    val appstats = jsonAppStats.parseJson.convertTo[List[AppStats]](spray.json.DefaultJsonProtocol.listFormat[AppStats](itmo.escience.streamsimulator.utilities.StatsJsonProtocol.appStatsFormat))
//
//    val engine = new SDEngine[NodeOperatorContainerBasedScheduleImproved]()
//    engine.build(ctx, StatsUtils.mergeStats(appstats))
//    val resultSolution = engine.run(50, 300)
//    val resultSchedule = SDProblem.solutionToScheduleImproved(resultSolution, ctx)
//    println("Finished")
//
//  }
//
//  private def readAllLines(path: Path): List[String] = {
//    val reader = new BufferedReader(new FileReader(path.toString))
//    val lines = reader.lines().iterator().asScala.toList
//    reader.close()
//    lines
//  }
//
//}
