import sbt._
import Keys._
import Process._

object ILDLBuild extends Build {

  // http://stackoverflow.com/questions/6506377/how-to-get-list-of-dependency-jars-from-an-sbt-0-10-0-project
  val getJars = TaskKey[Unit]("get-jars")
  val getJarsTask = getJars <<= (target, fullClasspath in Runtime) map { (target, cp) =>
    println("Target path is: "+target)
    println("Full classpath is: "+cp.map(_.data).mkString(":"))
  }

  val defaults = Defaults.defaultSettings ++ Seq(
    scalaSource in Compile := baseDirectory.value / "src",
    javaSource in Compile := baseDirectory.value / "src",
    scalaSource in Test := baseDirectory.value / "test",
    javaSource in Test := baseDirectory.value / "test",
    resourceDirectory in Compile := baseDirectory.value / "resources",
    compileOrder := CompileOrder.Mixed,

    unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
    unmanagedSourceDirectories in Test := Seq((scalaSource in Test).value),
    //http://stackoverflow.com/questions/10472840/how-to-attach-sources-to-sbt-managed-dependencies-in-scala-ide#answer-11683728
    com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys.withSource := true,

    scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-Xlint"),

    // reflect is a first-class dependency
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,

    parallelExecution in Global := false
  )

  val runtimeDeps = Seq[Setting[_]]()

  val pluginDeps = Seq(
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value
  )

  val scalaMeter = {
    val scalaMeter  = Seq("com.github.axel22" %% "scalameter" % "0.5-M2")
    val scalaMeterFramework = new TestFramework("org.scalameter.ScalaMeterFramework")
    Seq(
      libraryDependencies ++= scalaMeter, 
      testFrameworks += scalaMeterFramework,
      testOptions in ThisBuild += Tests.Argument(scalaMeterFramework, "-silent", "-preJDK7")
    )
  }

  val junitDeps: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.10-M2" % "test"
    ),
    parallelExecution in Test := false,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
  )

  val pluginCompilationDeps: Seq[Setting[_]] = Seq(
    fork in Test := true,
    scalacOptions in Compile <++= (Keys.`package` in (plugin, Compile)) map { (jar: File) =>
      val addPlugin = "-Xplugin:" + jar.getAbsolutePath
      // Thanks Jason for this cool idea (taken from https://github.com/retronym/boxer)
      // add plugin timestamp to compiler options to trigger recompile of
      // main after editing the plugin. (Otherwise a 'clean' is needed.)
      val dummy = "-Jdummy=" + jar.lastModified
      Seq(addPlugin, dummy)
    }
  )

  val testsDeps: Seq[Setting[_]] = junitDeps ++ Seq(
    getJarsTask,
    fork in Test := true,
    javaOptions in Test <+= (dependencyClasspath in Runtime, scalaBinaryVersion, packageBin in Compile in plugin) map { (path, ver, _) =>
      def isBoot(file: java.io.File) = 
        ((file.getName() startsWith "scala-") && (file.getName() endsWith ".jar")) ||
        (file.toString contains ("target/scala-" + ver)) // this makes me cry, seriously sbt...

      val cp = "-Xbootclasspath/a:" + path.map(_.data).filter(isBoot).mkString(":")
      // println(cp)
      cp
    },
    libraryDependencies ++= (
      if (scalaVersion.value.startsWith("2.10")) {
        Seq(
          "org.scala-lang" % "scala-partest" % scalaVersion.value, 
          "com.googlecode.java-diff-utils" % "diffutils" % "1.3.0"
        )
      } else {
        Seq(
          "org.scala-lang.modules" %% "scala-partest" % "1.0.0",
          "com.googlecode.java-diff-utils" % "diffutils" % "1.3.0"
        )
      }
    )
  )

  lazy val _ildl       = Project(id = "ildl",             base = file("."),                      settings = defaults) aggregate (runtime, plugin, tests)
  lazy val runtime     = Project(id = "ildl-runtime",     base = file("components/runtime"),     settings = defaults)
  lazy val plugin      = Project(id = "ildl-plugin",      base = file("components/plugin"),      settings = defaults ++ pluginDeps) dependsOn(runtime)
  lazy val tests       = Project(id = "ildl-tests",       base = file("tests/correctness"),      settings = defaults ++ pluginDeps ++ testsDeps) dependsOn(plugin, runtime)
  lazy val benchmarks  = Project(id = "ildl-benchmarks",  base = file("tests/benchmarks"),       settings = defaults ++ runtimeDeps ++ scalaMeter ++ pluginCompilationDeps) dependsOn(plugin, runtime)
}
