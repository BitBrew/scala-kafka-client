import com.typesafe.sbt.SbtGit.git
import scala.util.matching.Regex

// Versioning
enablePlugins(com.typesafe.sbt.GitVersioning)
val VersionRegex: Regex = "v([.0-9]+)-?(.*)?".r
lazy val versioningSettings = Seq(
  git.useGitDescribe := true,
  git.baseVersion := "0.0.0",
  git.uncommittedSignifier := None,
  git.gitTagToVersionNumber := {
    case VersionRegex(v, "")         => Some(v)
    case VersionRegex(v, "SNAPSHOT") => Some(s"$v-SNAPSHOT")
    case VersionRegex(v, s)          => Some(s"$v-$s-SNAPSHOT")
    case x => None
  }
)

lazy val commonSettings = Seq(
  organization := "net.cakesolutions",
  scalaVersion := "2.12.1",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  publishMavenStyle := true,
  //bintrayOrganization := Some("cakesolutions"),
  //bintrayPackageLabels := Seq("scala", "kafka"),
//  resolvers += "Apache Staging" at "https://repository.apache.org/content/groups/staging/",
  scalacOptions in Compile ++= Seq(
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xlint",
    "-Xfuture",
    "-Ywarn-dead-code",
    "-Ywarn-unused-import",
    "-Ywarn-unused",
    "-Ywarn-nullary-unit"
  ),
  scalacOptions in (Compile, doc) ++= Seq("-groups", "-implicits"),
  javacOptions in (Compile, doc) ++= Seq("-notimestamp", "-linksource"),
  autoAPIMappings := true,

  //  publishTo :=
  //TODO publish snapshots to OSS
  //  if (Version.endsWith("-SNAPSHOT"))
  //    Seq(
  //      publishTo := Some("Artifactory Realm" at "http://oss.jfrog.org/artifactory/oss-snapshot-local"),
  //      bintrayReleaseOnPublish := false,
  //      // Only setting the credentials file if it exists (#52)
  //      credentials := List(Path.userHome / ".bintray" / ".artifactory").filter(_.exists).map(Credentials(_))
  //    )
  //  else

  publishTo := {
    val nexus = "https://nexus.bitbrew.com/"
    if (isSnapshot.value)
      Some("BitBrew Nexus Snapshots" at nexus + "repository/libs-snapshot-local")
    else
      Some("BitBrew Nexus Releases" at  nexus + "repository/libs-release-local")
  },

  (for {
    username <- Option(System.getenv().get("CI_DEPLOY_USERNAME"))
    password <- Option(System.getenv().get("CI_DEPLOY_PASSWORD"))
  } yield credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.bitbrew.com", username,
    password)).getOrElse { credentials += Credentials(Path.userHome / ".nexus" / ".credentials") },

  parallelExecution in Test := false,
  parallelExecution in IntegrationTest := true,

  publishArtifact in Test := false,

  pomExtra := <scm>
    <url>git@github.com:cakesolutions/scala-kafka-client.git</url>
    <connection>scm:git:git@github.com:cakesolutions/scala-kafka-client.git</connection>
  </scm>
    <developers>
      <developer>
        <id>simon</id>
        <name>Simon Souter</name>
        <url>https://github.com/simonsouter</url>
      </developer>
      <developer>
        <id>jkpl</id>
        <name>Jaakko Pallari</name>
        <url>https://github.com/jkpl</url>
      </developer>
    </developers>,

  licenses := ("MIT", url("http://opensource.org/licenses/MIT")) :: Nil
) ++ versioningSettings

lazy val kafkaTestkit = project.in(file("testkit"))
  .settings(commonSettings: _*)

lazy val scalaKafkaClient = project.in(file("client"))
  .settings(commonSettings: _*)
  .dependsOn(kafkaTestkit % "test")
  .configs(IntegrationTest extend Test)

lazy val scalaKafkaClientAkka = project.in(file("akka"))
  .settings(commonSettings: _*)
  .dependsOn(scalaKafkaClient)
  .dependsOn(kafkaTestkit % "test")
  .configs(IntegrationTest extend Test)

lazy val scalaKafkaClientExamples = project.in(file("examples"))
  .settings(commonSettings: _*)
  .dependsOn(scalaKafkaClientAkka)

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(unidocSettings: _*)
  .settings(name := "scala-kafka-client-root", publishArtifact := false, publish := {}, publishLocal := {})
  .aggregate(scalaKafkaClient, scalaKafkaClientAkka, kafkaTestkit)
