//file: project/ProjectSyntax.scala

object ProjectSyntax {
  import sbt._
  import sbt.Keys._
  import Configs._

  //XXX val mainDependencyManagement = "compile->compile;test->compile,test;it->compile,test;at->compile,test;ft->compile,test;pt->compile,test;tools->compile,test"
  //XXX val testDependencyManagement = "compile;test;it;ft;at;pt;tools"

  implicit class ImplicitProjectSyntax(project: sbt.Project) {
    implicit def inConfigs(cs: Configuration*): sbt.Project = {
      val compile: Set[Configuration] = Set(Compile)
      val scopes = cs.filterNot(c => compile.contains(c)).map(c => c.name).mkString(";")
      val p =
        project
          .configs(cs:_*)
          .settings(
            testFrameworks += new TestFramework("scalaprops.ScalapropsFramework"),
            testFrameworks += new TestFramework("utest.runner.Framework"),
            testFrameworks += new TestFramework("munit.Framework"),
            libraryDependencies ++=
              Seq(
                "com.github.scalaprops" %% "scalaprops" % "0.9.0"  % scopes,
                "org.scalameta"         %% "munit"      % "0.7.29" % scopes,
                "com.lihaoyi"           %% "utest"      % "0.7.11" % scopes))
      val excludes: Set[Configuration] = Set(Compile, Test)
      cs
        .foldLeft(p) { (acc, item) =>
          if(excludes.contains(item))
            acc
          else
            acc.settings(
              inConfig(item)(
                Defaults.itSettings ++
                  Seq(
                    unmanagedSourceDirectories   ++= (Test / sourceDirectories  ).value,
                    unmanagedResourceDirectories ++= (Test / resourceDirectories).value,
                    dependencyClasspath := (dependencyClasspath).value ++ (Test/exportedProducts).value)))
        }
    }
  }

  def inPlaceTests(c: Configuration): Seq[Setting[_]]  = forkSettings(c, false, false)
  def forkedTests(c: Configuration): Seq[Setting[_]]   = forkSettings(c, true, false)
  def parallelTests(c: Configuration): Seq[Setting[_]] = forkSettings(c, true, true)
  def forkSettings(c: Configuration, forked: Boolean, parallel: Boolean): Seq[Setting[_]] =
    inConfig(c)(
      Seq(
        fork              := forked,
        parallelExecution := parallel))
}

object Configs {
  import sbt._

  val FunctionalTest  = Configuration.of("FunctionalTest",  "ft") extend (Test)
  val AcceptanceTest  = Configuration.of("AcceptanceTest",  "at") extend (Test)
  val PerformanceTest = Configuration.of("PerformanceTest", "pt") extend (Test)
  val Tools           = Configuration.of("Tools",        "tools") extend (Test)
}
