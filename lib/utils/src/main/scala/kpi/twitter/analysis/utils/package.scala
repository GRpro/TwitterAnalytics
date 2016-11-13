package kpi.twitter.analysis

import com.typesafe.config.{Config, ConfigFactory}

package object utils {

  /**
    * kpi.twitter.analysis.BuildInfo class is auto generated before
    * compilation phase by all other modules that depend on this one.
    */
  lazy val buildInfo = Class.forName("kpi.twitter.analysis.BuildInfo")
    .newInstance
    .asInstanceOf[{ val info: Map[String, String] }]
    .info

  def getOptions(fileName: String = "application.conf"): Config =
    ConfigFactory.load(fileName)

}
