
name := "ildl"

scalaVersion in Global := "2.11.6"

version in Global := "0.1-SNAPSHOT"

organization in Global := "org.scala-miniboxing.plugins"

organizationName in Global := "LAMP/EPFL"

organizationHomepage in Global := Some(url("http://lamp.epfl.ch"))

licenses in Global := Seq("BSD-style" -> url("http://www.scala-lang.org/license.html"))

homepage in Global := Some(url("http://lamp.epfl.ch"))

resolvers in ThisBuild ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
