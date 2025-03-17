ThisBuild / scalaVersion := "3.6.4"

val fs2Version = "3.11.0"

lazy val `fs2-intro` = project
  .in(file("."))
  .settings(
    // so that cats-effect doesn't complain about threads
    Compile / run / fork := true,
    // so that console input works when running as a fork
    Compile / run / connectInput := true,
    scalapbCodeGeneratorOptions ++= List(
      CodeGeneratorOption.FlatPackage,
      CodeGeneratorOption.Scala3Sources),
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "dev.kovstas" %% "fs2-throttler" % "1.0.13",
      "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion))
  .enablePlugins(Fs2Grpc)
