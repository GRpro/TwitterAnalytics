import sbt.Keys._
import dependencies._
import sbtassembly.AssemblyPlugin.autoImport._

javacOptions ++= Seq("-encoding", "UTF-8")

val buildInfoSettings = Seq(
  sourceGenerators in Compile <+= (sourceManaged in Compile, version, name) map { (d, v, n) =>
    val file = d / "info.scala"
    IO.write(file, """package kpi.twitter.analysis
                     |class BuildInfo {
                     |  val info = Map[String, String](
                     |    "name" -> "%s",
                     |    "version" -> "%s"
                     |    )
                     |}
                     |""".stripMargin.format(n, v))
    Seq(file)
  }
)

val commonSettings = Seq(
  organization := "kpi.twitter.analysis",
  version := s"${Process("git describe --tags").lines.head}",
  scalaVersion := "2.11.8",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs@_*) => MergeStrategy.discard
    case x => (assemblyMergeStrategy in assembly).value(x)
  }
)

/*
 * Project definitions
 */

// root
lazy val TwitterAnalytics = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "TwitterAnalytics"
  )
  .aggregate(utils, consumer, analyzer, webapp, tools)

lazy val utils = project.in(file("lib/utils"))
  .settings(commonSettings: _*)
  .settings(
    name := "TwitterAnalytics-utils",
    libraryDependencies := utilsDependencies
  )

lazy val consumer = project.in(file("consumer"))
  .settings(commonSettings: _*)
  .settings(buildInfoSettings: _*)
  .settings(
    name := "TwitterAnalytics-consumer",
    libraryDependencies := consumerDependencies
  )
  .enablePlugins(AssemblyPlugin)
  .dependsOn(utils)

lazy val analyzer = project.in(file("analyzer"))
  .settings(commonSettings: _*)
  .settings(buildInfoSettings: _*)
  .settings(
    name := "TwitterAnalytics-analyzer"
  )
  .dependsOn(utils)

lazy val webapp = project.in(file("webapp"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(buildInfoSettings: _*)
  .settings(
    name := "TwitterAnalytics-webapp",
    libraryDependencies ++= webappDependencies
  )
  .dependsOn(utils)

lazy val tools = project.in(file("tools"))
  .settings(commonSettings: _*)
  .settings(
    name := "TwitterAnalytics-tools",
    libraryDependencies ++= toolsDependencies
  )
