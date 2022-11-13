import mill._, scalalib._
import modules.Jvm

trait PackageIt extends ScalaModule {
  import mill.api.Ctx
  import java.nio.file.Files
  import java.nio.file.attribute.PosixFilePermission
  import Jvm._

  def launcherUniversalScript(
      mainClass: String,
      shellClassPath: Agg[String],
      cmdClassPath: Agg[String],
      jvmArgs: Seq[String],
      shebang: Boolean = false
  ): String = {
    universalScript(
      shellCommands =
        s"""app_home=`dirname "$$0"`\n exec java ${jvmArgs.mkString(" ")} $$JAVA_OPTS -cp "${shellClassPath.iterator.mkString(
            ":"
          )}" '$mainClass' "$$@"""",
      cmdCommands =
        s"""java ${jvmArgs.mkString(" ")} %JAVA_OPTS% -cp "${cmdClassPath.iterator.mkString(
            ";"
          )}" $mainClass %*""",
      shebang = shebang
    )
  }

 def createLauncher(mainClass: String, classPath: Agg[os.Path], jvmArgs: Seq[String])(implicit
      ctx: Ctx.Dest
  ): PathRef = {
    val isWin = scala.util.Properties.isWin
    val isBatch =
      isWin && !(org.jline.utils.OSUtils.IS_CYGWIN || org.jline.utils.OSUtils.IS_MSYSTEM)
    val outputPath = ctx.dest / (if (isBatch) "run.bat" else "run")
    //val classPathStrs = classPath.map(_.toString)
    val classPathStrs = classPath.map{path => 
      "${app_home}" + java.io.File.separator + ".." + java.io.File.separator + path.subRelativeTo(path / os.up / os.up).toString
    }

    os.write(outputPath, launcherUniversalScript(mainClass, classPathStrs, classPathStrs, jvmArgs))

    if (!isWin) {
      val perms = Files.getPosixFilePermissions(outputPath.toNIO)
      perms.add(PosixFilePermission.GROUP_EXECUTE)
      perms.add(PosixFilePermission.OWNER_EXECUTE)
      perms.add(PosixFilePermission.OTHERS_EXECUTE)
      Files.setPosixFilePermissions(outputPath.toNIO, perms)
    }
    PathRef(outputPath)
  }

  def packageIt = T {
    val dest = T.ctx().dest
    val libDir = dest / "lib"
    val binDir = dest / "bin"

    os.makeDir(libDir)
    os.makeDir(binDir)

    val allJars = runClasspath()
      .map(_.path)
      .filter(path => os.exists(path) && !os.isDir(path))
      .toSeq

    allJars.foreach { file =>
      os.copy.into(file, libDir)
    }

    Jvm.createJar(localClasspath().map(_.path).filter(os.exists), manifest())
    os.move( dest / "out.jar", libDir / s"${artifactName()}.jar")

    //val runnerFile = Jvm.createLauncher(
    val runnerFile = createLauncher(
        finalMainClass(),
        Agg.from(os.list(libDir)),
        forkArgs()
    )

    os.move.into(runnerFile.path, binDir)

    PathRef(dest)
  }

  def packageItRun() = T.command {
    val p = packageIt().path
    os.proc(p + "/bin/run").call(stdout = os.Inherit, stderr = os.Inherit)
  }
}
