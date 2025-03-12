ThisBuild / scalaVersion := "3.6.4"

val fs2Version = "3.11.0"

lazy val `fs2-intro` = project
  .in(file("."))
  .settings(
    Compile / run / fork := true,
    Compile / run / connectInput := true,
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "dev.kovstas" %% "fs2-throttler" % "1.0.13",
      "org.typelevel" %% "fs2-grpc-runtime" % "2.7.21",
      "io.grpc" % "grpc-netty-shaded" % "1.71.0"))
