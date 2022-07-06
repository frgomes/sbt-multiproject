val application = "sbt-multiproject-demo"
organization := "mathminds.io"
name := application

//XXX ThisBuild / credentials += findCredentials
//XXX def findCredentials: Credentials = {
//XXX   sys.env.get("SYSTEM_ACCESSTOKEN") match {
//XXX     case Some(key) => Credentials("", artifactBase, artifactTree, key)
//XXX     case None => Credentials(Path.userHome / ".sbt" / ".credentials" / ThisBuild.organization.value / ThisBuild.name.value)
//XXX   }
//XXX }

ThisBuild / versionScheme := Some("early-semver")
ThisBuild / versionPolicyIntention := Compatibility.BinaryAndSourceCompatible

ThisBuild / publishMavenStyle         := true
ThisBuild / publishConfiguration      := publishConfiguration.value.withOverwrite(false)
ThisBuild / publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
//FIXME: ThisBuild / publishTo := Some(artifactProject at artifactURL)

ThisBuild / assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs@_*) =>
    (xs map {
      _.toLowerCase
    }) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) => MergeStrategy.discard
      case _ => MergeStrategy.discard
    }
  case _ => MergeStrategy.first
}
Global / excludeLintKeys += ThisBuild / assembly / assemblyMergeStrategy

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

val versionOf: Map[String,String] =
  s"""|java=1.8
      |scala.v2.13=2.13.8
      |scala.v2.12=2.12.16
      |spark.v3.3=3.3.0
      |spark.v3.2=3.2.1
      |spark.v3.1=3.1.2
      |""".stripMargin
    .split("\n")
    .toSeq
    .map(s => s.split("="))
    .map { case Array(k: String, v: String) => Map(k -> v) }
    .reduce(_ ++ _)

val scala213 = versionOf("scala.v2.13")
val scala212 = versionOf("scala.v2.12")

val spark33 = versionOf("spark.v3.3")
val spark32 = versionOf("spark.v3.2")
val spark31 = versionOf("spark.v3.1")

//XXX initialize := {
//XXX   val _ = initialize.value // run the previous initialization
//XXX   val required = versionOf("java")
//XXX   val current  = sys.props("java.specification.version")
//XXX   assert(current == required, s"Unsupported JDK: java.specification.version ${current} != ${required}")
//XXX }

def buildinfoSettings: Seq[Setting[_]] =
  Seq(
    buildInfoPackage := s"${organization.value}.buildinfo".replace("-", "."),
    buildInfoKeys    := Seq[BuildInfoKey](organization, name, version, scalaVersion, sbtVersion))


def disablePublishing: Seq[Setting[_]] =
  Seq(
    publish/skip := true,
    publishLocal/skip := true,
  )

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

val Regex = "([0-9]+).([0-9]+).([0-9]+)".r

def sparkDependencies(version: String, scope: Configuration = Provided): Seq[Setting[_]] =
  Seq(
    libraryDependencies ++= {
      version match {
        case Regex("3", minor, _) if (minor.toInt > 1) => 
          Seq(
            "org.apache.spark" %% "spark-core" % version  % scope,
            "org.apache.spark" %% "spark-sql"  % version  % scope,
            "com.databricks"   %% "spark-xml"  % "0.14.0" % "test;it;ft;pt;at")
        case Regex("3", minor, _) if (minor.toInt <= 1) => 
          Seq(
            "org.apache.spark" %% "spark-core" % version  % scope,
            "org.apache.spark" %% "spark-sql"  % version  % scope,
            "com.databricks"   %% "spark-xml"  % "0.13.0" % "test;it;ft;pt;at")
        case _ =>
          throw new IllegalArgumentException("Invalid Spark version ${version}")
      }
    }
  )

def scalaSettings: Seq[Setting[_]] =
  Seq(
    scalacOptions := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if(n <= 12) => scalac212
        case Some((2, n)) if(n > 12)  => scalac213
        case Some((3, _)) => scalac213
        case Some((epoch, major)) =>
          throw new IllegalArgumentException("Invalid Scala version ${epoch}.${major}")
      }})

val scalacCommon: Seq[String] =
  Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    //FIXME: "-Xfatal-warnings",
    //FIXME: "-Wconf:any:error",
  )

val scalac213: Seq[String] =
  scalacCommon ++
    Seq(
      "-target:jvm-1.8",
      "-Ytasty-reader", // https://docs.scala-lang.org/scala3/guides/migration/compatibility-classpath.html
      "-Xlint:-byname-implicit", // https://github.com/scala/bug/issues/12072
      "-Xlint:-missing-interpolator,-unused,_",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
    )

val scalac212: Seq[String] =
  scalacCommon ++
    Seq(
      "-target:jvm-1.8",
      "-Xfuture",
      //XXX "-Xlint:-byname-implicit", // https://github.com/scala/bug/issues/12072
      "-Xlint:-missing-interpolator,-unused,_",
      "-Yno-adapted-args",
      "-Ypartial-unification",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-unused-import",
      "-Ywarn-value-discard",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
    )

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import Configs._
import ProjectSyntax._

def testSettings(scopes: String): Seq[Setting[_]] =
  Seq(
    testFrameworks += new TestFramework("scalaprops.ScalapropsFramework"),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    testFrameworks += new TestFramework("munit.Framework"),
    javaOptions ++= Seq("-Xmx1G", "-ea"),
    fork := true,
    parallelExecution := false,
    libraryDependencies ++=
      Seq(
        "com.github.scalaprops" %% "scalaprops" % "0.9.0"  % scopes,
        "org.scalameta"         %% "munit"      % "0.7.29" % scopes,
        "com.lihaoyi"           %% "utest"      % "0.7.11" % scopes))

lazy val root =
  project
    .in(file("."))
    .settings(disablePublishing)
    .settings(crossScalaVersions := Nil)
    .aggregate(`root_spark33`, `root_spark32` , `root_spark31`)

lazy val `root_spark33` =
  project
    .in(file("target/.root_spark33"))
    .settings(disablePublishing)
    .aggregate(`core_spark33`)

lazy val `root_spark32` =
  project
    .in(file("target/.root_spark32"))
    .settings(disablePublishing)
    .aggregate(`core_spark32`)

lazy val `root_spark31` =
  project
    .in(file("target/.root_spark31"))
    .settings(disablePublishing)
    .aggregate(`core_spark31`)

lazy val `core_spark33` =
  project
    .in(file("target/.core_spark33"))
    .settings(name := s"${application}_core_spark33")
    .settings(sourceDirectory := (sourceDirectory.value / "/../../.." / "core/src").getCanonicalFile())
    .inConfigs(Test,IntegrationTest,FunctionalTest,AcceptanceTest,PerformanceTest)(testSettings)
    .settings(crossScalaVersions := Seq(scala212, scala213))
    .settings(scalaSettings)
    .settings(sparkDependencies(spark33))
    .settings(buildinfoSettings).enablePlugins(BuildInfoPlugin)

lazy val `core_spark32` =
  project
    .in(file("target/.core_spark32"))
    .settings(name := s"${application}_core_spark32")
    .settings(sourceDirectory := (sourceDirectory.value / "/../../.." / "core/src").getCanonicalFile())
    .inConfigs(Test,IntegrationTest,FunctionalTest,AcceptanceTest,PerformanceTest)(testSettings)
    .settings(crossScalaVersions := Seq(scala212, scala213))
    .settings(scalaSettings)
    .settings(sparkDependencies(spark32))
    .settings(buildinfoSettings).enablePlugins(BuildInfoPlugin)

lazy val `core_spark31` =
  project
    .in(file("target/.core_spark31"))
    .settings(name := s"${application}_core_spark31")
    .settings(sourceDirectory := (sourceDirectory.value / "/../../.." / "core/src").getCanonicalFile())
    .inConfigs(Test,IntegrationTest,FunctionalTest,AcceptanceTest,PerformanceTest)(testSettings)
    .settings(crossScalaVersions := Seq(scala212))
    .settings(scalaSettings)
    .settings(sparkDependencies(spark31))
    .settings(buildinfoSettings).enablePlugins(BuildInfoPlugin)
