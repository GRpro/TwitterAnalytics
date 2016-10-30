import sbt.Keys._
import dependencies._


val buildVersion = "1.0"

val commonSettings = Seq(
  organization := "kpi.twitter.analysis",
  version := s"$buildVersion-${Process("git rev-parse HEAD").lines.head}",
  scalaVersion := "2.11.8"
)

// root
lazy val TwitterAnalytics = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "TwitterAnalytics"
  )
  .aggregate(utils, consumer, ml_model, analyzer)

lazy val utils = project.in(file("lib/utils"))
  .settings(commonSettings: _*)
  .settings(
    name := "TwitterAnalytics-utils",
    libraryDependencies := utilsDependencies
  )

lazy val consumer = project.in(file("consumer"))
  .settings(commonSettings: _*)
  .settings(
    name := "TwitterAnalytics-consumer",
    libraryDependencies := consumerDependencies
  )
  .dependsOn(utils)

lazy val ml_model = project.in(file("ml_model"))
  .settings(commonSettings: _*)
  .settings(
    name := "TwitterAnalytics-ml_model"
  )
  .dependsOn(utils)

lazy val analyzer = project.in(file("analyzer"))
  .settings(commonSettings: _*)
  .settings(
    name := "TwitterAnalytics-analyzer"
  )
  .dependsOn(utils)



