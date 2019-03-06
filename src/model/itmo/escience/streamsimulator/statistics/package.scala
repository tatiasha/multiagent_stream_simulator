package itmo.escience.streamsimulator

/**
  * Created by nikolay on 02.03.17.
  */
package object statistics {
  /**
    * The first element of the pairs are timestamp
    * The second is a coressponfing aggregated quantity
    */
  type CountPerWindow = (Long, Long)
  type MeanPerWindow = (Long, Double)
}
