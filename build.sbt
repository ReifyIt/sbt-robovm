def robovmVersion = "1.0.0-alpha-01"

organization := "it.reify"

name := "sbt-robovm"

version := robovmVersion + "-SNAPSHOT"

description := "SBT plugin for building Scala iOS apps with RoboVM"

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("http://sbt-robovm.reify.it"))

sbtPlugin := true

libraryDependencies ++= Seq(
  "org.robovm" % "robovm-cacerts-full" % robovmVersion,
  "org.robovm" % "robovm-cocoatouch"   % robovmVersion,
  "org.robovm" % "robovm-compiler"     % robovmVersion,
  "org.robovm" % "robovm-objc"         % robovmVersion,
  "org.robovm" % "robovm-rt"           % robovmVersion)

// Doc settings

scalacOptions in (Compile, doc) ++= {
  val tagOrBranch = if (version.value.endsWith("-SNAPSHOT")) "master" else "v" + version.value
  val docSourceUrl = "https://github.com/reifyit/sbt-robovm/tree/" + tagOrBranch + "€{FILE_PATH}.scala"
  Seq("-sourcepath", (baseDirectory in LocalProject("sbt-robovm")).value.getAbsolutePath,
      "-doc-source-url", docSourceUrl)
}

// Publish settings

pomIncludeRepository := (_ => false)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := {
  <scm>
    <url>git@github.com:reifyit/sbt-robovm.git</url>
    <connection>scm:git:git@github.com:reifyit/sbt-robovm.git</connection>
  </scm>
  <developers>
    <developer>
      <id>c9r</id>
      <name>Chris Sachs</name>
      <email>chris@reify.it</email>
    </developer>
  </developers>
}
