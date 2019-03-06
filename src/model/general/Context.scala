package model.general

import java.util

import model.entities.Tenant

/**
  * Created by mikhail on 23.01.2017.
  */
case class Context[TSchedule <: Schedule](protected var _tenants: List[Tenant],
              protected var _env: Environment,
              protected var _workload: Workload,
              protected var _schedule: TSchedule,
              protected var _time: Double,
              protected var _framework: Framework
             ) extends Cloneable {

  def tenants: List[Tenant] = _tenants

  def env: Environment = _env
  def env_=(value: Environment): Unit = { _env = value }

  def workload: Workload = _workload
  def workload_=(value: Workload): Unit = { _workload = value }

  def schedule: TSchedule = _schedule
  def schedule_=(value: TSchedule): Unit = { _schedule = value }

  def time: Double = _time
  def time_=(value: Double): Unit = { _time = value }

  def framework: Framework = _framework
  def framework_=(value: Framework): Unit = { _framework = value }

  def add(tenant: Tenant): Unit = {
    if (!_tenants.contains(tenant)) {
      _tenants :+= tenant
    }
  }

  def remove(tenant: Tenant): Unit = {
    if (!_tenants.contains(tenant)) {
      throw new RuntimeException(s"Tenant ${tenant.id} doesn't exist")
    }
    _tenants = _tenants.filter(_ != tenant)
  }

  override def clone(): Context[TSchedule] = {
    Context(this._tenants.map(x => x.clone()), this._env.clone(),
      this._workload.clone().asInstanceOf[Workload], this._schedule.clone().asInstanceOf[TSchedule], this._time, this._framework.clone())
  }
}
