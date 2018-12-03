import sbt._

object ResolverSettings {
  lazy val projectResolvers: Seq[Resolver] = Seq(
    "BitBrew Maven Remote"        at "https://nexus.bitbrew.com/repository/maven-remote/",
    "BitBrew Ivy Remote"          at "https://nexus.bitbrew.com/repository/ivy-remote/",
    "BitBrew Libs Snapshots"      at "https://nexus.bitbrew.com/repository/libs-snapshot-local/",
    "BitBrew Libs Releases"       at "https://nexus.bitbrew.com/repository/libs-release-local/",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.typesafeRepo("releases"),
    Resolver.typesafeRepo("snapshots"),
    JavaNet2Repository,
    Resolver.bintrayRepo("dnvriend", "maven"),
    Resolver.bintrayRepo("krasserm", "maven"),
    Resolver.bintrayRepo("rbmhtechnology", "maven"),
    Resolver.bintrayRepo("scalaz", "releases"),
    Resolver.bintrayRepo("outworkers", "oss-releases"),
    "Twitter Repository"          at "http://maven.twttr.com",
    "Confluent Maven Repo"        at "http://packages.confluent.io/maven/",
    Resolver.bintrayRepo("cakesolutions", "maven"),
    "justwrote" at "http://repo.justwrote.it/releases/"
  )

  lazy val projectCredentials: Credentials = {
    val credentials = for {
      username <- sys.env.get("CI_DEPLOY_USERNAME")
      password <- sys.env.get("CI_DEPLOY_PASSWORD")
    } yield Credentials("Sonatype Nexus Repository Manager", "nexus.bitbrew.com", username, password)

    credentials.getOrElse { Credentials(Path.userHome / ".nexus" / ".credentials") }
  }
}