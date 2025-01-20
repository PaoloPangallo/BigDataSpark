package Logica;

import org.apache.spark.sql.{Dataset, Row}

object FileUtils {

  import scala.io.Source
  import java.io.{File, FileWriter}

  def saveAsSingleJson(df: Dataset[Row], outputPath: String, fileName: String): Unit = {
    val tempDir = s"$outputPath-temp" // Directory temporanea
    val targetFilePath = s"$outputPath\\$fileName" // File di destinazione

    // Salva come JSON
    df.coalesce(1).write.mode("overwrite").json(tempDir)

    // Trova il file part-00000
    val tempDirFile = new File(tempDir)
    val partFile = waitForPartFile(tempDirFile)

    // Copia il contenuto nel file di destinazione
    if (partFile.isDefined) {
      val source = Source.fromFile(partFile.get)
      val content = source.getLines().mkString("\n")
      source.close()

      val writer = new FileWriter(targetFilePath)
      writer.write(content)
      writer.close()

      // Rimuove la directory temporanea
      tempDirFile.listFiles().foreach(_.delete())
      tempDirFile.delete()
    } else {
      throw new IllegalStateException(s"File part-00000 non trovato nella directory $tempDir.")
    }
  }

  private def waitForPartFile(tempDirFile: File): Option[File] = {
    var retries = 10
    var partFile: Option[File] = None

    while (retries > 0 && partFile.isEmpty) {
      partFile = tempDirFile.listFiles().find(_.getName.startsWith("part-"))
      if (partFile.isEmpty) {
        Thread.sleep(500)
        retries -= 1
      }
    }

    partFile
  }
}

