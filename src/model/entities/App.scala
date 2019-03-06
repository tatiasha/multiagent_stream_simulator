package model.entities

import java.util

import model.{UserId, AppId}

/**
  * Created by mikhail on 23.01.2017.
  */
case class App(id: AppId, userId: UserId, operators: List[Operator])
  extends Cloneable with Serializable {

  override def clone(): App = {
    App(this.id, this.userId, this.operators.map(x => x.clone()))
  }
}
