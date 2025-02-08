import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.sql.functions._
import org.apache.log4j.{Level, Logger}

object ChessMLPredictor {

  @transient lazy val logger: Logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {
    logger.info("Inizio esecuzione del ChessMLPredictor")
    val spark = createSparkSession()

    val filePath = "C:/Users/paolo/Desktop/Big Data/chess_games.csv/chess_games.csv"

    val chessData = loadDataset(spark, filePath)
    logger.info(s"Dataset caricato con ${chessData.count()} partite.")

    // Prepariamo i dati per il Machine Learning
    val preparedData = prepareData(chessData)

    val Array(trainingData, testData) = preparedData.randomSplit(Array(0.8, 0.2))

    // Addestriamo il modello
    val model = trainModel(trainingData)

    // Testiamo il modello
    evaluateModel(model, testData)

    // Salviamo il modello
    model.write.overwrite().save("C:/Users/paolo/Desktop/ProgettoSparkScacchi/Output/ml_model")

    logger.info("Modello addestrato e salvato con successo!")
    spark.stop()
  }

  /** Crea una sessione Spark con le configurazioni specificate */
  def createSparkSession(): SparkSession = {
    logger.info("Creazione della Spark session con le configurazioni specificate...")
    val session = SparkSession.builder()
      .appName("Chess ML Predictor")
      .master("local[*]")
      .config("spark.driver.memory", "8g")
      .config("spark.executor.memory", "8g")
      .config("spark.driver.extraJavaOptions", "--add-opens=java.base/java.nio=ALL-UNNAMED --add-exports=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED")
      .config("spark.executor.extraJavaOptions", "--add-opens=java.base/java.nio=ALL-UNNAMED --add-exports=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED")
      .config("spark.serializer", "org.apache.spark.serializer.JavaSerializer")
      .config("spark.eventLog.enabled", "true")
      .config("spark.eventLog.dir", "file:///tmp/spark-events")
      .config("spark.history.fs.logDirectory", "file:///tmp/spark-events")
      .config("spark.kryo.registrationRequired", "false")
      .config("spark.kryoserializer.buffer.max", "1024m")
      .config("spark.sql.inMemoryColumnarStorage.compressed", "true")
      .config("spark.sql.inMemoryColumnarStorage.batchSize", "10000")
      .config("spark.network.timeout", "600s")
      .getOrCreate()
    logger.info("Spark session creata correttamente.")
    session
  }

  /** Carica il dataset in un DataFrame */
  def loadDataset(spark: SparkSession, filePath: String): Dataset[Row] = {
    logger.info("Caricamento del dataset in DataFrame...")
    val df = spark.read.option("header", "true")
      .option("inferSchema", "true")
      .csv(filePath)
    logger.info("Dataset caricato nel DataFrame.")
    df
  }

  /** Prepara i dati per il Machine Learning */
  private def prepareData(data: Dataset[Row]): Dataset[Row] = {
    logger.info("Inizio preparazione dei dati...")
    val spark = data.sparkSession
    import spark.implicits._

    val cleanedData = data.na.fill(Map(
      "WhiteRatingDiff" -> 0.0,
      "BlackRatingDiff" -> 0.0
    ))

    val finalData = cleanedData
      .withColumn("UTCTime", col("UTCTime").cast("string"))
      .drop("AN")

    val resultIndexer = new StringIndexer()
      .setInputCol("Result")
      .setOutputCol("label")
    val labeledData = resultIndexer.fit(finalData).transform(finalData)

    val topOpenings = labeledData.groupBy("Opening").count()
      .orderBy(desc("count"))
      .limit(50)
      .select("Opening")

    val frequentOpeningsSet = topOpenings.as[String].collect().toSet
    val broadcastTopOpenings = spark.sparkContext.broadcast(frequentOpeningsSet)

    val categorizeOpening = udf((opening: String) => {
      if (broadcastTopOpenings.value.contains(opening)) opening else "Other"
    })

    val groupedData = labeledData.withColumn("OpeningGrouped", categorizeOpening(col("Opening")))

    val openingIndexer = new StringIndexer()
      .setInputCol("OpeningGrouped")
      .setOutputCol("OpeningIndex")
    val indexedData = openingIndexer.fit(groupedData).transform(groupedData)

    val categorizeTimeControl = udf((tc: String) => {
      tc match {
        case s if s.matches("(30|60|120|180)\\+.*")  => "Bullet"
        case s if s.matches("(300|600)\\+.*")        => "Blitz"
        case s if s.matches("(900|1200|1800)\\+.*")  => "Rapid"
        case s if s.matches("(3600|7200)\\+.*")      => "Classical"
        case _                                       => "Other"
      }
    })

    val reducedTimeControl = indexedData.withColumn("TimeCategory", categorizeTimeControl(col("TimeControl")))

    val timeControlIndexer = new StringIndexer()
      .setInputCol("TimeCategory")
      .setOutputCol("TimeIndex")
    val indexedTimeData = timeControlIndexer.fit(reducedTimeControl).transform(reducedTimeControl)

    val assembler = new VectorAssembler()
      .setInputCols(Array("WhiteElo", "BlackElo", "OpeningIndex", "WhiteRatingDiff", "BlackRatingDiff", "TimeIndex"))
      .setOutputCol("features")

    val assembledData = assembler.transform(indexedTimeData)

    logger.info("Preparazione dei dati completata con successo.")
    assembledData.select("features", "label")
  }

  /** Allena il modello di Machine Learning */
  private def trainModel(trainingData: Dataset[Row]): RandomForestClassificationModel = {
    logger.info("Inizio addestramento del modello RandomForest...")
    val model = new RandomForestClassifier()
      .setLabelCol("label")
      .setFeaturesCol("features")
      .setNumTrees(50)
      .setMaxDepth(5)
      .setMaxBins(128)
    val trainedModel = model.fit(trainingData)
    logger.info("Modello addestrato con successo.")
    trainedModel
  }

  /** Valuta l'accuratezza del modello */
  private def evaluateModel(model: RandomForestClassificationModel, testData: Dataset[Row]): Unit = {
    logger.info("Inizio valutazione del modello...")
    val predictions = model.transform(testData)
    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")
    val accuracy = evaluator.evaluate(predictions)
    logger.info(s"📊 Accuratezza del modello: $accuracy")
  }
}
