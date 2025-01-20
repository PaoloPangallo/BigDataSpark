import java.io.File

object FileUtils {

  // Funzione per rinominare il file part-00000 in un nome leggibile
  def renameGeneratedFile(tempDir: String, finalFileName: String): String = {
    val tempFile = new File(s"$tempDir/part-00000")  // Percorso del file part-00000
    if (tempFile.exists()) {
      val finalFile = new File(s"$tempDir/$finalFileName") // Nuovo nome file
      if (tempFile.renameTo(finalFile)) {
        println(s"File rinominato in: ${finalFile.getAbsolutePath}")
        return finalFile.getAbsolutePath
      } else {
        throw new IllegalStateException(s"Impossibile rinominare il file: ${tempFile.getAbsolutePath}")
      }
    } else {
      throw new IllegalStateException(s"Il file ${tempFile.getAbsolutePath} non esiste.")
    }
  }
}
