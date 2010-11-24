import sbt._

class ImplicitRestProject(info: ProjectInfo) extends DefaultWebProject(info) {
  val liftVersion = "2.1"
  
  // deployment
  override def managedStyle = ManagedStyle.Maven
  override def jettyWebappPath = webappPath 
  override def scanDirectories = Nil 
  
  val webkit = "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default"
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.21" % "test"
  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
  val scalatoolsSnapshot = "Scala Tools Snapshot" at "http://scala-tools.org/repo-snapshots/"
}
