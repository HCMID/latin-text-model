
organization := "edu.holycross.shot.mid"
name := "latin-text-model"

version := "1.0.0"

crossScalaVersions in ThisBuild := Seq("2.11.8", "2.12.4")
scalaVersion := (crossScalaVersions in ThisBuild).value.last


licenses += ("GPL-3.0",url("https://opensource.org/licenses/gpl-3.0.html"))

resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("neelsmith", "maven")


libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" %  "test",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",


  "edu.holycross.shot.cite" %% "xcite" % "3.2.2",
  "edu.holycross.shot" %% "ohco2" % "10.5.4",
  "edu.holycross.shot" %% "orca" % "4.0.1"
)

enablePlugins(TutPlugin)
