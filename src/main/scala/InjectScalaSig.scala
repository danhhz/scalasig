import org.apache.bcel.Repository
import org.apache.bcel.classfile.{JavaClass, Unknown}
import org.apache.bcel.generic.ClassGen
import org.apache.bcel.util.{ClassPath, SyntheticRepository}
import java.io.File

object InjectScalaSig {
  def main(args: Array[String]): Unit = {
    val filesByFolder = args.map(new File(_)).groupBy(_.getAbsoluteFile.getParent)
    filesByFolder.foreach({ case (folder, classFiles) => {
      val classPath = new ClassPath(folder)
      val repository = SyntheticRepository.getInstance(classPath)
      classFiles.map(_.getName.replace(".class", "")).foreach(className => {
        val clazz = repository.loadClass(className)
        val outputPath = classPath.getClassFile(className).getPath
        maybeInject(clazz, outputPath)
      })
    }})
  }

  def maybeInject(clazz: JavaClass, outputPath: String): Unit = {
    // TODO(dan): Flesh this out.
    val hasScalaSignature = true
    val hasScalaSig = false
    if (!hasScalaSignature) {
      println("Skipping %s - no @ScalaSignature".format(outputPath))
    } else if (hasScalaSig) {
      println("Skipping %s - has ScalaSig".format(outputPath))
    } else {
      inject(clazz, outputPath)
    }
  }

  def inject(clazz: JavaClass, outputPath: String): Unit = {
    val gen = new ClassGen(clazz)
    val pool = gen.getConstantPool
    val nameIndex = pool.addUtf8("ScalaSig")
    val fpool = pool.getFinalConstantPool
    gen.addAttribute(new Unknown(nameIndex, 3, Array[Byte](5, 0, 0), fpool))
    println(outputPath)
    gen.getJavaClass().dump(outputPath)
  }
}
