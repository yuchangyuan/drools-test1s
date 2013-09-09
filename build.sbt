// -*- mode: scala -*
import AssemblyKeys._

organization := "me.ycy"

name := "drools-test1"

scalaVersion := "2.10.2"

version := "0.1"

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("javax", xs @ _*)          => MergeStrategy.first
    case PathList("org", "xmlpull", xs @ _*) => MergeStrategy.first
    case PathList("org", "w3c", xs @ _*)     => MergeStrategy.first
    case "application.conf"                  => MergeStrategy.concat
    case x                                   => old(x)
  }
}

libraryDependencies ++= Seq(
  "org.drools" % "drools-core" % "5.5.0.Final",
  "org.drools" % "drools-compiler" % "5.5.0.Final",
  "com.sun.xml.bind" % "jaxb-xjc" % "2.2.4-1",
  "org.drools" % "knowledge-api" % "5.5.0.Final",
  "ch.qos.logback" % "logback-classic" % "1.0.9" % "runtime",
  "ch.qos.logback" % "logback-core" % "1.0.9" % "runtime"
)
