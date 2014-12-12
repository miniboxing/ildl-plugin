
name := "ildl"

scalaVersion in Global := "2.11.4"

version in Global := "0.0-SNAPSHOT"

organization in Global := "ch.epfl.lamp"

organizationName in Global := "LAMP/EPFL"

organizationHomepage in Global := Some(url("http://lamp.epfl.ch"))

licenses in Global := Seq("BSD-style" -> file("LICENSE"))

homepage in Global := Some(url("http://lamp.epfl.ch"))

resolvers in ThisBuild ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
