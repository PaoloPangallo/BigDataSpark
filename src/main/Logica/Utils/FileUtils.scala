package utils

import java.io.{File, FileWriter}
import scala.io.Source
import org.apache.spark.sql.{Dataset, Row}

object FileUtils {

  // Salva un Dataset come JSON a file singolo
  def saveAsSingleJson(df: Dataset[Row], outputPath: String, fileName: String): Unit = {
    val tempDir = s"$outputPath-temp" // Directory temporanea per il salvataggio
    val targetDir = new File(outputPath) // Directory di destinazione
    val targetFilePath = s"$outputPath\\$fileName" // Percorso del file di destinazione

    // Crea la directory di destinazione se non esiste
    if (!targetDir.exists() && !targetDir.mkdirs()) {
      throw new IllegalStateException(s"Impossibile creare la directory: ${targetDir.getAbsolutePath}")
    }

    // Salva il dataset come partizione singola
    df.coalesce(1)
      .write
      .mode("overwrite")
      .json(tempDir)

    // Trova il file JSON generato
    val tempDirFile = new File(tempDir)
    if (!tempDirFile.exists() || !tempDirFile.isDirectory) {
      throw new IllegalStateException(s"La directory temporanea $tempDir non esiste o non è una directory valida.")
    }

    val partFile = waitForPartFile(tempDirFile)
    if (partFile.isEmpty) {
      throw new IllegalStateException(s"File part-00000 non trovato nella directory $tempDir.")
    }

    // Leggi il contenuto del file generato
    val source = Source.fromFile(partFile.get)
    val content = source.getLines().mkString("\n")
    source.close()

    // Scrivi il contenuto nel file di destinazione
    val writer = new FileWriter(targetFilePath)
    writer.write(content)
    writer.close()

    // Rimuove la directory temporanea
    tempDirFile.listFiles().foreach(_.delete())
    tempDirFile.delete()
  }

  // Metodo per attendere che il file part-00000 venga generato
  def waitForPartFile(tempDirFile: File): Option[File] = {
    var retries = 10 // Numero massimo di tentativi
    var partFile: Option[File] = None

    while (retries > 0 && partFile.isEmpty) {
      partFile = tempDirFile.listFiles().find(_.getName.startsWith("part-"))
      if (partFile.isEmpty) {
        Thread.sleep(500) // Attendi 500ms prima di riprovare
        retries -= 1
      }
    }

    partFile
  }
}
