import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

object ChessMLPredictor {

  def main(args: Array[String]): Unit = {
    val spark = createSparkSession()

    // Percorso del dataset
    val filePath = "C:/Users/paolo/Desktop/Big Data/chess_games.csv/chess_games.csv"

    // Carichiamo il dataset
    val chessData = loadDataset(spark, filePath)
    println(s"Dataset caricato con ${chessData.count()} partite.")

    // Prepariamo i dati per il Machine Learning
    val preparedData = prepareData(chessData)

    // Divide il dataset in train/test
    val Array(trainingData, testData) = preparedData.randomSplit(Array(0.8, 0.2))

    // Addestriamo il modello
    val model = trainModel(trainingData)

    // Testiamo il modello
    evaluateModel(model, testData)

    // Salviamo il modello
    model.write.overwrite().save("C:/Users/paolo/Desktop/ProgettoSparkScacchi/Output/ml_model")

    println("Modello addestrato e salvato con successo!")
    spark.stop()
  }

  /** Crea una sessione Spark con opzioni JVM e memoria per Java 17 */
  def createSparkSession(): SparkSession = {
    SparkSession.builder()
      .appName("Chess ML Predictor")
      .master("local[*]")
      // Imposta la memoria per driver ed executor (modifica in base alle risorse disponibili)
      .config("spark.driver.memory", "8g")
      .config("spark.executor.memory", "8g")
      // Opzioni JVM per le restrizioni dei moduli in Java 17
      .config("spark.driver.extraJavaOptions", "--add-opens=java.base/java.nio=ALL-UNNAMED --add-exports=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED")
      .config("spark.executor.extraJavaOptions", "--add-opens=java.base/java.nio=ALL-UNNAMED --add-exports=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED")
      .config("spark.serializer", "org.apache.spark.serializer.JavaSerializer")
      .config("spark.kryo.registrationRequired", "false")
      .config("spark.kryoserializer.buffer.max", "1024m")
      .config("spark.sql.inMemoryColumnarStorage.compressed", "true")
      .config("spark.sql.inMemoryColumnarStorage.batchSize", "10000")
      .config("spark.network.timeout", "600s")
      .getOrCreate()
  }

  /** Carica il dataset in un DataFrame */
  def loadDataset(spark: SparkSession, filePath: String): Dataset[Row] = {
    spark.read.option("header", "true")
      .option("inferSchema", "true")
      .csv(filePath)
  }

  /**
   * Prepara i dati per il Machine Learning:
   * 1. Gestisce i valori nulli;
   * 2. Converte UTCTime in stringa e rimuove la colonna "AN";
   * 3. Converte "Result" in label numerica;
   * 4. Raggruppa la feature "Opening": manteniamo solo le top 31 aperture e tutte le altre diventano "Other";
   * 5. Trasforma "openingGrouped" in indice numerico;
   * 6. Assembla le feature.
   */
  private def prepareData(data: Dataset[Row]): Dataset[Row] = {
    val spark = data.sparkSession
    import spark.implicits._

    // 1. Sostituiamo i valori nulli per le colonne numeriche
    val cleanedData = data.na.fill(Map(
      "WhiteRatingDiff" -> 0.0,
      "BlackRatingDiff" -> 0.0
    ))

    // 2. Convertiamo UTCTime in stringa e rimuoviamo "AN"
    val finalData = cleanedData
      .withColumn("UTCTime", col("UTCTime").cast("string"))
      .drop("AN")

    // 3. Convertiamo "Result" in label numerica
    val resultIndexer = new StringIndexer()
      .setInputCol("Result")
      .setOutputCol("label")
    val labeledData = resultIndexer.fit(finalData).transform(finalData)

    // 4. Raggruppiamo la feature "Opening"
    // Calcoliamo la frequenza di ogni apertura
    val openingFreq = labeledData.groupBy("Opening").count()
    // Selezioniamo le top 31 aperture (le più frequenti)
    val topN = 31
    val frequentOpenings = openingFreq.orderBy(desc("count")).limit(topN).select("Opening")
    val frequentOpeningsSet = frequentOpenings.as[String].collect().toSet
    // Broadcast per efficienza
    val broadcastSet = spark.sparkContext.broadcast(frequentOpeningsSet)
    // UDF: se l'apertura non è tra le top, la raggruppiamo come "Other"
    val groupOpeningUDF = udf((opening: String) => {
      if (broadcastSet.value.contains(opening)) opening else "Other"
    })
    val withGroupedOpening = labeledData.withColumn("openingGrouped", groupOpeningUDF(col("Opening")))

    // 5. Trasformiamo "openingGrouped" in un indice numerico
    val openingIndexer = new StringIndexer()
      .setInputCol("openingGrouped")
      .setOutputCol("openingIndex")
    val indexedData = openingIndexer.fit(withGroupedOpening).transform(withGroupedOpening)

    // 6. Assembliamo le feature (le altre feature numeriche + l'indice per "Opening")
    val assembler = new VectorAssembler()
      .setInputCols(Array("WhiteElo", "BlackElo", "openingIndex", "WhiteRatingDiff", "BlackRatingDiff"))
      .setOutputCol("features")
    val assembledData = assembler.transform(indexedData)

    // Selezioniamo le colonne necessarie per il training
    assembledData.select("features", "label")
  }

  /** Allena il modello di Machine Learning */
  private def trainModel(trainingData: Dataset[Row]): RandomForestClassificationModel = {
    // Con la nuova gestione, la cardinalità di "openingIndex" sarà al massimo 32 (31 + "Other")
    val model = new RandomForestClassifier()
      .setLabelCol("label")
      .setFeaturesCol("features")
      .setNumTrees(50)    // Numero di alberi
      .setMaxDepth(5)
    model.fit(trainingData)
  }

  /** Valuta l'accuratezza del modello */
  private def evaluateModel(model: RandomForestClassificationModel, testData: Dataset[Row]): Unit = {
    val predictions = model.transform(testData)
    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")
    val accuracy = evaluator.evaluate(predictions)
    println(s"📊 Accuratezza del modello: $accuracy")
  }
}
