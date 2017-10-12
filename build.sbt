
organization := "edu.holycross.shot.mid"
name := "latin-text-model"

version := "0.0.1"

scalaVersion := "2.12.3"
//crossScalaVersions := Seq("2.11.8", "2.12.1")
crossScalaVersions := Seq("2.11.8")

licenses += ("GPL-3.0",url("https://opensource.org/licenses/gpl-3.0.html"))

resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("neelsmith", "maven")


libraryDependencies ++= Seq(
  "edu.holycross.shot.cite" %% "xcite" % "3.2.1",
  "edu.holycross.shot" %% "ohco2" % "10.3.0",
  "edu.holycross.shot" %% "orca" % "3.0.0",

  "org.scalatest" %% "scalatest" % "3.0.1" %  "test",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
)
