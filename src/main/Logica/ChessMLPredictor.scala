import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.ml.classification._
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.sql.functions._
import org.apache.log4j.{Level, Logger}

object ChessMLPredictor {

  @transient lazy val logger: Logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {
    logger.info("Inizio esecuzione del ChessMLPredictorLogistic")
    val spark = createSparkSession()

    val filePath = "C:/Users/paolo/Desktop/Big Data/chess_games.csv/chess_games.csv"

    val chessData = loadDataset(spark, filePath)
    logger.info(s"Dataset caricato con ${chessData.count()} partite.")

    val preparedData = prepareData(chessData)

    val Array(trainingData, testData) = preparedData.randomSplit(Array(0.8, 0.2))

    val modelType = "RandomForest"  // Cambia tra "LogisticRegression", "RandomForest", "GBT", "DecisionTree"
    val model = trainModel(trainingData, modelType)

    evaluateModel(model, testData, modelType)


    logger.info(s"Modello $modelType addestrato e salvato con successo!")
    spark.stop()
  }

  /** Crea una sessione Spark */
  def createSparkSession(): SparkSession = {
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

  /** Carica il dataset */
  def loadDataset(spark: SparkSession, filePath: String): Dataset[Row] = {
    val df = spark.read.option("header", "true")
      .option("inferSchema", "true")
      .csv(filePath)
    df
  }

  /** Prepara i dati */
  private def prepareData(data: Dataset[Row]): Dataset[Row] = {
    val spark = data.sparkSession
    import spark.implicits._

    val labeledData = data.withColumn("label",
      when($"Result" === "1-0", 0.0)
        .when($"Result" === "0-1", 1.0)
        .otherwise(2.0)
    )

    val cleanedData = labeledData.na.fill(Map(
      "WhiteRatingDiff" -> 0.0,
      "BlackRatingDiff" -> 0.0
    ))

    // 🔹 Raggruppiamo le aperture meno comuni in "Other"
    val topOpenings = cleanedData.groupBy("Opening").count()
      .orderBy(desc("count"))
      .limit(50)

    val frequentOpeningsSet = topOpenings.select("Opening").as[String].collect().toSet
    val broadcastTopOpenings = spark.sparkContext.broadcast(frequentOpeningsSet)

    val categorizeOpening = udf((opening: String) => {
      if (broadcastTopOpenings.value.contains(opening)) opening else "Other"
    })

    val groupedData = cleanedData.withColumn("OpeningGrouped", categorizeOpening(col("Opening")))

    val openingIndexer = new StringIndexer()
      .setInputCol("OpeningGrouped")
      .setOutputCol("OpeningIndex")
    val indexedData = openingIndexer.fit(groupedData).transform(groupedData)

    val timeControlIndexer = new StringIndexer()
      .setInputCol("TimeControl")
      .setOutputCol("TimeIndex")
    val indexedTimeData = timeControlIndexer.fit(indexedData).transform(indexedData)

    val assembler = new VectorAssembler()
      .setInputCols(Array("WhiteElo", "BlackElo", "OpeningIndex", "WhiteRatingDiff", "BlackRatingDiff", "TimeIndex"))
      .setOutputCol("features")

    val assembledData = assembler.transform(indexedTimeData)

    assembledData.select("features", "label")
  }

  /** Allena il modello scelto */
  private def trainModel(trainingData: Dataset[Row], modelType: String): ClassificationModel[_ , _] = {
    modelType match {
      case "LogisticRegression" =>
        new LogisticRegression()
          .setLabelCol("label")
          .setFeaturesCol("features")
          .setMaxIter(100)
          .setRegParam(0.01)
          .setElasticNetParam(0.8)
          .fit(trainingData)

      case "RandomForest" =>
        new RandomForestClassifier()
          .setLabelCol("label")
          .setFeaturesCol("features")
          .setNumTrees(50)
          .setMaxDepth(5)
          .setMaxBins(1024)  // 🔹 Risolto il problema maxBins
          .fit(trainingData)

      case "GBT" =>
        new GBTClassifier()
          .setLabelCol("label")
          .setFeaturesCol("features")
          .setMaxIter(50)
          .fit(trainingData)

      case "DecisionTree" =>
        new DecisionTreeClassifier()
          .setLabelCol("label")
          .setFeaturesCol("features")
          .setMaxBins(1024)  // 🔹 Risolto il problema maxBins
          .fit(trainingData)

      case _ => throw new IllegalArgumentException("Modello non riconosciuto")
    }
  }

  /** Valuta l'accuratezza del modello e genera la matrice di confusione */
  private def evaluateModel(model: ClassificationModel[_ , _], testData: Dataset[Row], modelType: String): Unit = {
    val predictions = model.transform(testData)

    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")

    val accuracy = evaluator.evaluate(predictions)
    logger.info(s"📊 Accuratezza del modello $modelType: $accuracy")

    val confusionMatrix = predictions.groupBy("label", "prediction").count()
      .orderBy("label", "prediction")

    logger.info(s"📊 Matrice di Confusione per $modelType:")
    confusionMatrix.show()

    confusionMatrix.write
      .option("header", "true")
      .csv(s"C:/Users/paolo/Desktop/ProgettoSparkScacchi/Output/confusion_matrix_$modelType.csv")
  }
}
