//file: project/ProjectSyntax.scala

object ProjectSyntax {
  import sbt._
  import sbt.Keys._
  import Configs._

  //XXX val mainDependencyManagement = "compile->compile;test->compile,test;it->compile,test;at->compile,test;ft->compile,test;pt->compile,test;tools->compile,test"
  //XXX val testDependencyManagement = "compile;test;it;ft;at;pt;tools"

  implicit class ImplicitProjectSyntax(project: sbt.Project) {
    implicit def inConfigs(cs: Configuration*)(settings: String => Seq[Setting[_]]): sbt.Project = {
      val compile: Set[Configuration] = Set(Compile)
      val scopes = cs.filterNot(c => compile.contains(c)).map(c => c.name).mkString(";")
      val p =
        project
          .configs(cs:_*)
          .settings(settings(scopes))
      val excludes: Set[Configuration] = Set(Compile, Test)
      cs
        .foldLeft(p) { (acc, item) =>
          if(excludes.contains(item))
            acc
          else
            //XXX if(item.id == IntegrationTest.id) {
            //XXX   acc.settings(
            //XXX     Seq(
            //XXX       IntegrationTest/unmanagedSourceDirectories   ++= (Test / sourceDirectories  ).value,
            //XXX       IntegrationTest/unmanagedResourceDirectories ++= (Test / resourceDirectories).value,
            //XXX       IntegrationTest/dependencyClasspath := (Test/dependencyClasspath).value ++ (Test/exportedProducts).value))
            //XXX } else {
              acc.settings(
                inConfig(item)(
                  Defaults.itSettings ++
                    Seq(
                      unmanagedSourceDirectories   ++= (Test / sourceDirectories  ).value,
                      unmanagedResourceDirectories ++= (Test / resourceDirectories).value,
                      dependencyClasspath := (Test/dependencyClasspath).value ++ (Test/exportedProducts).value)))
            //XXX }
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

  //XXX val IntegrationTest = Configuration.of("IntegrationTest", "it") extend (Test)
  val FunctionalTest  = Configuration.of("FunctionalTest",  "ft") extend (Test)
  val AcceptanceTest  = Configuration.of("AcceptanceTest",  "at") extend (Test)
  val PerformanceTest = Configuration.of("PerformanceTest", "pt") extend (Test)
  val Tools           = Configuration.of("Tools",        "tools") extend (Test)
}
