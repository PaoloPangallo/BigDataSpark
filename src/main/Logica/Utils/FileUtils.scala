package Utils

import org.apache.spark.sql.{Dataset, Row}
import java.io.{File, IOException}

object FileUtils {

  def saveAndRename(data: Dataset[Row], outputDir: String, targetFileName: String): Unit = {
    try {
      println(s"Scrittura dei dati in: $outputDir")
      data.coalesce(1)
        .write
        .mode("overwrite")
        .option("header", "true")
        .option("quote", "\"")       // Configura l'uso delle virgolette
        .option("escape", "\"")      // Configura l'escape per le virgolette
        .csv(outputDir)

      renameCSVFile(outputDir, targetFileName)
    } catch {
      case e: IOException =>
        println(s"Errore durante la scrittura in $outputDir: ${e.getMessage}")
        throw e
    }
  }

  private def renameCSVFile(sourceDir: String, targetFileName: String): Unit = {
    val sourceDirFile = new File(sourceDir)
    if (sourceDirFile.exists() && sourceDirFile.isDirectory) {
      val partFile = sourceDirFile.listFiles().find(_.getName.startsWith("part-"))
      partFile match {
        case Some(file) =>
          val renamedFile = new File(targetFileName)
          if (renamedFile.exists() && !renamedFile.delete()) {
            throw new IOException(s"Impossibile eliminare il file esistente: $targetFileName")
          }
          if (!file.renameTo(renamedFile)) {
            throw new IOException(s"Errore nel rinominare il file: ${file.getName} -> $targetFileName")
          }
        case None =>
          println(s"Nessun file part trovato nella directory $sourceDir")
      }
    } else {
      println(s"La directory $sourceDir non esiste o non è una directory.")
    }
  }

  def deleteDirectory(directory: File): Boolean = {
    if (directory.isDirectory) {
      directory.listFiles().foreach(deleteDirectory)
    }
    directory.delete()
  }
}
