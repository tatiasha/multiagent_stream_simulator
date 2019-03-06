//package itmo.escience.streamsimulator.experiments.baseintegration
//
//import itmo.escience.streamsimulator.algorithms.ga.GAScheduler
//import itmo.escience.streamsimulator.algorithms.mockschedulers.MockScheduler
//import itmo.escience.streamsimulator.experiments.Experiment
//import itmo.escience.streamsimulator.utilities.JSONParser
//
///**
//  * Created by mikhail on 30.01.2017.
//  */
//class GABaseIntegrationExp(var inputPath: String = "", var outputPath: String = "") extends Experiment {
//  def run() = {
//    if (inputPath == "") {
//      inputPath = getClass.getClassLoader.getResource("jsonInOutExample").getPath() + "/input_example.json"
//    }
//    if (outputPath == "") {
//      outputPath = ".\\temp\\output_result.json"
//    }
//    val ctx = JSONParser.readInputJson(inputPath)
//    val scheduler = new GAScheduler()
//    val resCtx = scheduler.schedule(ctx).head
//    JSONParser.writeOutput(outputPath, resCtx)
//    println("finish")
//  }
//}
