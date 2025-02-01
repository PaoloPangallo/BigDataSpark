package Utils

import org.apache.spark.sql.{Dataset, Row}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.conf.Configuration
import org.apache.log4j.Logger
import java.io.IOException

object FileUtils {
  private val log = Logger.getLogger(getClass)

  /** Salva i dati in formato Parquet e rinomina il file */
  def saveAndRenameParquet(data: Dataset[Row], outputDir: String, targetFileName: String): Unit = {
    try {
      log.info(s"Salvataggio in formato Parquet: $outputDir")

      // Salva i dati in Parquet con una sola partizione
      data.repartition(1)
        .write
        .mode("overwrite")
        .parquet(outputDir)

      renameFile(outputDir, targetFileName, "parquet")

    } catch {
      case e: IOException =>
        log.error(s"Errore durante la scrittura in Parquet: ${e.getMessage}")
        throw e
    }
  }

  /** Salva i dati in formato CSV e rinomina il file */
  def saveAndRename(data: Dataset[Row], outputDir: String, targetFileName: String): Unit = {
    try {
      log.info(s"Scrittura dei dati in: $outputDir")

      // Salva i dati in CSV con una sola partizione
      data.repartition(1)
        .write
        .mode("overwrite")
        .option("header", "true")
        .csv(outputDir)

      // Rinomina il file CSV generato
      renameFile(outputDir, targetFileName, "csv")

    } catch {
      case e: IOException =>
        log.error(s"Errore durante la scrittura in CSV: ${e.getMessage}")
        throw e
    }
  }

  /** Rinomina il file generato da Spark (Parquet o CSV) */
  private def renameFile(sourceDir: String, targetFileName: String, fileExtension: String): Unit = {
    val conf = new Configuration()
    val fs = FileSystem.get(conf)
    val sourcePath = new Path(sourceDir)
    val targetPath = new Path(targetFileName)

    // Trova il file part-*
    val files = fs.listStatus(sourcePath).map(_.getPath)
    val partFile = files.find(_.getName.startsWith("part-"))

    partFile match {
      case Some(file) =>
        if (fs.exists(targetPath)) fs.delete(targetPath, true)
        fs.rename(file, targetPath)
        log.info(s"File ${fileExtension.toUpperCase} rinominato con successo: ${file.getName} -> $targetFileName")
      case None =>
        log.warn(s"Nessun file $fileExtension trovato nella directory $sourceDir")
    }
  }
}
