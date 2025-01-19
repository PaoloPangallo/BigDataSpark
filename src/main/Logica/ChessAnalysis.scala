import org.apache.spark.sql.SparkSession
import java.io.File

object ChessAnalysis {
  def main(args: Array[String]): Unit = {
    // Inizializza una SparkSession con configurazione per disabilitare le librerie native di Hadoop
    val spark = SparkSession.builder()
      .appName("Chess Dataset Test")
      .master("local[*]") // Usa tutte le CPU disponibili in locale
      .config("spark.hadoop.io.native.lib.available", "false") // Disabilita librerie native Hadoop
      .getOrCreate()

    // Percorso del file di input
    val filePath = "C:\\Users\\paolo\\Desktop\\Big Data\\chess_games.csv\\chess_games.csv"

    // Leggi il dataset di scacchi
    val chessData = spark.read
      .option("header", "true") // Il dataset ha un'intestazione
      .option("inferSchema", "true") // Inferisce automaticamente lo schema
      .csv(filePath)

    // Mostra le prime righe del dataset per verificare il caricamento
    println("Prime righe del dataset:")
    chessData.show(10)

    // Mostra lo schema del dataset per verificare i tipi di dati
    println("Schema del dataset:")
    chessData.printSchema()



    // Crea la directory di output se non esiste
    val outputDir = new File("C:\\Users\\paolo\\Desktop\\ProgettoSparkScalaScacchi\\Output")
    if (!outputDir.exists()) outputDir.mkdirs()

    // Percorso del file di output
    val outputPath = "C:\\Users\\paolo\\Desktop\\ProgettoSparkScalaScacchi\\Output\\winCounts.json"

    // Salva i risultati in JSON


    // Arresta la SparkSession
    spark.stop()
  }
}
