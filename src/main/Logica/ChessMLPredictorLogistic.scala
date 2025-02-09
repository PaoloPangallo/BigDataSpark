
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.ml.classification._
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.evaluation.{MulticlassClassificationEvaluator, BinaryClassificationEvaluator}
import org.apache.spark.ml.Transformer
import org.apache.spark.sql.functions._
import org.apache.log4j.Logger

import org.knowm.xchart.{CategoryChart, CategoryChartBuilder, BitmapEncoder}
import scala.collection.JavaConverters._

object ChessMLPredictorLogistic {

  @transient lazy val logger: Logger = Logger.getLogger(getClass.getName)

  case class ModelMetrics(
                           modelName: String,
                           trainingTime: Double,
                           evaluationTime: Double,
                           trainingAccuracy: Double,
                           testAccuracy: Double,
                           f1: Double,
                           precision: Double,
                           recall: Double,
                           auc: Double,
                           overfitting: Double
                         )

  def main(args: Array[String]): Unit = {
    logger.info("Inizio esecuzione ChessMLPredictorAll")
    val spark = createSparkSession()

    // Caricamento del dataset
    val filePath = "C:/Users/paolo/Desktop/Big Data/chess_games.csv/chess_games.csv"
    val chessData = loadDataset(spark, filePath)
    logger.info(s"Dataset caricato con ${chessData.count()} partite.")

    // Preparazione dei dati
    val preparedData = prepareData(chessData)
    val Array(trainingData, testData) = preparedData.randomSplit(Array(0.8, 0.2))

    // Lista dei modelli da testare
    val models = Seq("RandomForest", "LogisticRegression", "GBT", "DecisionTree")

    // Per ogni modello: addestramento, valutazione e generazione della visualizzazione delle metriche
    models.foreach { modelType =>
      val (model, trainingTime) = trainModel(trainingData, modelType)
      val metrics = evaluateModel(model, trainingData, testData, modelType, trainingTime)
      logger.info(s"Metriche per $modelType: $metrics")
      generateMetricsBarChart(Map(
        "Training Accuracy" -> metrics.trainingAccuracy,
        "Test Accuracy" -> metrics.testAccuracy,
        "F1 Score" -> metrics.f1,
        "Precision" -> metrics.precision,
        "Recall" -> metrics.recall,
        "AUC" -> metrics.auc,
        "Overfitting" -> metrics.overfitting,
        "Training Time (s)" -> metrics.trainingTime,
        "Evaluation Time (s)" -> metrics.evaluationTime
      ), modelType)
    }

    logger.info("Modelli addestrati, valutati e visualizzati con successo!")
    spark.stop()
  }

  /** Creazione della sessione Spark */
  def createSparkSession(): SparkSession = {
    SparkSession.builder()
      .appName("Chess ML Predictor")
      .master("local[*]")
      .config("spark.driver.memory", "8g")
      .config("spark.executor.memory", "8g")
      .config("spark.eventLog.enabled", "true")
      .config("spark.eventLog.dir", "/tmp/spark-events")
      .getOrCreate()
  }

  /** Caricamento del dataset */
  def loadDataset(spark: SparkSession, filePath: String): Dataset[Row] = {
    spark.read.option("header", "true")
      .option("inferSchema", "true")
      .csv(filePath)
  }

  /** Preparazione dei dati */
  def prepareData(data: Dataset[Row]): Dataset[Row] = {
    val spark = data.sparkSession
    import spark.implicits._

    // Log iniziale: conteggio delle righe e distribuzione dei valori in "Result"
    logger.info("Conteggio totale righe nel dataset originale: " + data.count())
    data.groupBy("Result").count().show()

    // Conversione della colonna "Result" in etichetta numerica
    val labeledData = data.withColumn("label",
      when($"Result" === "1-0", 0.0)
        .when($"Result" === "0-1", 1.0)
        .otherwise(2.0)
    )
    logger.info("Conteggio righe dopo assegnazione label (prima del filtraggio): " + labeledData.count())

    // Filtraggio: rimuovi le righe che non sono 1-0 o 0-1
    val filteredData = labeledData.filter($"label" =!= 2.0)
    logger.info("Conteggio righe dopo filtraggio (solo 1-0 e 0-1): " + filteredData.count())

    // Riempimento dei valori nulli per alcune feature
    val cleanedData = filteredData.na.fill(Map(
      "WhiteRatingDiff" -> 0.0,
      "BlackRatingDiff" -> 0.0
    ))

    // Raggruppamento delle aperture meno frequenti in "Other"
    val topOpenings = cleanedData.groupBy("Opening").count()
      .orderBy(desc("count"))
      .limit(50)
    val frequentOpeningsSet = topOpenings.select("Opening").as[String].collect().toSet
    val broadcastTopOpenings = spark.sparkContext.broadcast(frequentOpeningsSet)
    val categorizeOpening = udf((opening: String) => {
      if (broadcastTopOpenings.value.contains(opening)) opening else "Other"
    })
    val groupedData = cleanedData.withColumn("OpeningGrouped", categorizeOpening(col("Opening")))

    // Trasformazioni: indicizzazione e assemblaggio
    val openingIndexer = new StringIndexer()
      .setInputCol("OpeningGrouped")
      .setOutputCol("OpeningIndex")
    val assembler = new VectorAssembler()
      .setInputCols(Array("WhiteElo", "BlackElo", "OpeningIndex", "WhiteRatingDiff", "BlackRatingDiff"))
      .setOutputCol("features")

    val indexerModel = openingIndexer.fit(groupedData)
    val indexedData = indexerModel.transform(groupedData)
    assembler.transform(indexedData).select("features", "label")
  }


  /** Addestramento del modello (misura anche il tempo di training) */
  def trainModel(trainingData: Dataset[Row], modelType: String): (Transformer, Double) = {
    logger.info(s"Addestramento modello: $modelType")
    val startTime = System.nanoTime()

    // Selezione e configurazione del modello
    val model = modelType match {
      case "RandomForest" =>
        new RandomForestClassifier()
          .setLabelCol("label")
          .setFeaturesCol("features")
          .setNumTrees(50)
          .setMaxDepth(5)
          .setMaxBins(64)
      case "LogisticRegression" =>
        new LogisticRegression()
          .setLabelCol("label")
          .setFeaturesCol("features")
          .setMaxIter(100)
          .setRegParam(0.01)
          .setElasticNetParam(0.8)
      case "GBT" =>
        new GBTClassifier()
          .setLabelCol("label")
          .setFeaturesCol("features")
          .setMaxIter(50)
          .setMaxBins(64)
      case "DecisionTree" =>
        new DecisionTreeClassifier()
          .setLabelCol("label")
          .setFeaturesCol("features")
          .setMaxDepth(5)
          .setMaxBins(64)
      case _ =>
        throw new IllegalArgumentException(s"Modello sconosciuto: $modelType")
    }

    val fittedModel = model.fit(trainingData)
    val endTime = System.nanoTime()
    val trainingTimeSeconds = (endTime - startTime) / 1e9
    logger.info(f"Tempo di addestramento per $modelType: $trainingTimeSeconds%.3f secondi")
    (fittedModel, trainingTimeSeconds)
  }


  def evaluateModel(model: Transformer,
                    trainingData: Dataset[Row],
                    testData: Dataset[Row],
                    modelType: String,
                    trainingTime: Double): ModelMetrics = {
    logger.info(s"Valutazione modello: $modelType")
    // Misura il tempo di evaluazione (solo sul test set)
    val evalStartTime = System.nanoTime()
    val predictionsTest = model.transform(testData)
    val evalEndTime = System.nanoTime()
    val evaluationTimeSeconds = (evalEndTime - evalStartTime) / 1e9

    // Evaluator per l'accuratezza e altre metriche
    val accuracyEvaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")
    val f1Evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("f1")
    val precisionEvaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("weightedPrecision")
    val recallEvaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("weightedRecall")
    // Binary evaluator per AUC (si applica poiché abbiamo solo due classi)
    val binaryEvaluator = new BinaryClassificationEvaluator()
      .setLabelCol("label")
      .setRawPredictionCol("rawPrediction")
      .setMetricName("areaUnderROC")

    // Calcolo delle metriche sul test set
    val testAccuracy = accuracyEvaluator.evaluate(predictionsTest)
    val f1 = f1Evaluator.evaluate(predictionsTest)
    val precision = precisionEvaluator.evaluate(predictionsTest)
    val recall = recallEvaluator.evaluate(predictionsTest)
    val auc = binaryEvaluator.evaluate(predictionsTest)

    // Calcolo delle metriche sul training set per misurare l'overfitting
    val predictionsTrain = model.transform(trainingData)
    val trainingAccuracy = accuracyEvaluator.evaluate(predictionsTrain)
    val overfitting = trainingAccuracy - testAccuracy

    // Visualizzazione della matrice di confusione
    logger.info(s"Matrice di Confusione per $modelType:")
    predictionsTest.groupBy("label", "prediction").count().show()

    logger.info(f"Metriche sul test per $modelType: accuracy = $testAccuracy%.3f, f1 = $f1%.3f, precision = $precision%.3f, recall = $recall%.3f, AUC = $auc%.3f")
    logger.info(f"Metriche sul training per $modelType: accuracy = $trainingAccuracy%.3f")
    logger.info(f"Differenza (overfitting measure) = $overfitting%.3f")
    logger.info(f"Tempo di valutazione per $modelType: $evaluationTimeSeconds%.3f secondi")

    ModelMetrics(
      modelName = modelType,
      trainingTime = trainingTime,
      evaluationTime = evaluationTimeSeconds,
      trainingAccuracy = trainingAccuracy,
      testAccuracy = testAccuracy,
      f1 = f1,
      precision = precision,
      recall = recall,
      auc = auc,
      overfitting = overfitting
    )
  }

  /** Genera e salva un grafico a barre delle metriche del modello usando XChart.
   *
   * Nota: per visualizzazioni più avanzate si potrebbero usare altre librerie o esportare i dati.
   */
  private def generateMetricsBarChart(metrics: Map[String, Double], modelName: String): Unit = {
    val chart: CategoryChart = new CategoryChartBuilder()
      .width(800)
      .height(600)
      .title(s"Metriche per $modelName")
      .xAxisTitle("Metrica")
      .yAxisTitle("Valore")
      .build()

    // Conversione delle chiavi in una lista Java
    val metricNames: java.util.List[String] = metrics.keys.toList.asJava

    // Soluzione 1: Creazione manuale della lista di Number
    val metricValues: java.util.List[Number] = new java.util.ArrayList[Number]()
    metrics.values.foreach(v => metricValues.add(java.lang.Double.valueOf(v)))

    chart.addSeries("Valori", metricNames, metricValues)

    val fileName = s"Metrics_$modelName.png"
    BitmapEncoder.saveBitmap(chart, fileName, BitmapEncoder.BitmapFormat.PNG)
    logger.info(s"Grafico delle metriche per $modelName salvato come $fileName")
  }


}
