import java.io._
import java.util
import java.util.Random
import java.util.logging.Level

import com.gargoylesoftware.htmlunit.{IncorrectnessListener, WebClient}
import com.gargoylesoftware.htmlunit.html._
import crawler.HtmlCrawler
import lucene.{FeatureExtraction, LuceneUtils}
import nvd.data.{DBConnection, NvdItemDao, RawDataProcess, SummaryExtraction}
import nvd.model.ProductSearch
import org.apache.commons.io.FileUtils
import org.apache.commons.logging.LogFactory
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.deeplearning4j.bagofwords.vectorizer.BagOfWordsVectorizer
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.text.sentenceiterator.{LineSentenceIterator, SentenceIterator}
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.{DefaultTokenizerFactory, TokenizerFactory}
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory

import collection.JavaConverters._
import w2v.W2VRNN
import weka.{ModelTrain, WekaUtils}
import weka.classifiers.{Classifier, Evaluation}
import weka.classifiers.functions.{LinearRegression, SimpleLinearRegression}
import weka.classifiers.meta.MultiClassClassifier
import weka.core.{Attribute, Instances, SerializationHelper}
import weka.core.converters.ConverterUtils.DataSource
import weka.filters.unsupervised.attribute.StringToNominal


/**
  * Created by ReggieYang on 2016/10/22.
  */
object Test {

  System.setProperty("hadoop.home.dir", "c:\\winutil\\")
  lazy val conf = new SparkConf().
    setMaster("local[10]").
    setAppName("My App").
    set("spark.cores.max", "10")
  lazy val sc = new SparkContext(conf)

  lazy val logger = LoggerFactory.getLogger(Test.getClass)

  def main(args: Array[String]) = {
    //    val conn = DBConnection.getConnection
    //    val se = new SummaryExtraction(conn)
    //    se.writeVectors()
    //    Array("impact", "amount", "category").foreach(WekaUtils.genArff(conn, _))
    //    WekaUtils.genArff(conn, "impact")
    //    WekaUtils.genArff(conn, "amount")
    //    WekaUtils.genArff(conn, "category")
    //    se.writeSummaryByProduct()
    //    conn.close()

    val mt = new ModelTrain
    val clsPath = "E:\\secdata\\wekaData\\model\\category_mcc.cls"
    //    mt.preprocess("E:\\secdata\\wekaData\\category.arff", new StringToNominal)
    //    mt.modelTrain("E:\\secdata\\wekaData\\category_nominal.arff", new MultiClassClassifier,
    //      "E:\\secdata\\wekaData\\model\\category_mcc.cls", 100)

    val trainDataPath = "E:\\secdata\\wekaData\\category_nominal.arff"
    val trainDataPath2 = "E:\\secdata\\wekaData\\player5.arff"

    val cls = new MultiClassClassifier
    val trainData = DataSource.read(trainDataPath2)
    val classIndex = trainData.numAttributes() - 1
    trainData.setClassIndex(classIndex)

    val eval = new Evaluation(trainData)
    eval.crossValidateModel(cls, trainData, 10, new Random(1))
    logger.info(eval.toSummaryString())
    SerializationHelper.write(s"E:\\secdata\\wekaData\\evaluation\\eval_mcc", eval)

    //    val eval1 = SerializationHelper.read("E:\\secdata\\wekaData\\evaluation\\eval_1").asInstanceOf[Evaluation]
    //    logger.info(eval1.toSummaryString())

    //   W2VRNN.trainModel("D:\\Documents\\glove.6B\\glove.6B.100d.txt", "D:\\Documents\\Downloads\\aclImdb_v1")
    //    W2VRNN.makeParVec()
    //    W2VRNN.parInfer()

  }

  def search() = {

    //    val rdd2: RDD[String] = sc.parallelize(p)
    //    rdd2.repartition(1).saveAsTextFile("data\\product_version")

    val productRDD = sc.textFile("data\\product_version")

    productRDD.foreachPartition(it => {
      val conn = DBConnection.getConnection
      val hc = new HtmlCrawler
      val nd = new NvdItemDao(conn)
      hc.init()
      var i = 0
      var tempPs = Array[ProductSearch]()
      it.foreach(product => {
        i = i + 1
        println(s"processing product: $product")
        val ps = ProductSearch(product, hc.crawlPage(hc.getBingRes(product)))
        tempPs = tempPs :+ ps
        if (i % 10 == 0) {
          nd.saveSearchRes(tempPs)
          tempPs = Array[ProductSearch]()
        }
      })
      nd.saveSearchRes(tempPs)
      conn.close()
    })
  }

  def w2v(corpus: String) = {
    val iter: SentenceIterator = new LineSentenceIterator(new File(corpus))
    val t: TokenizerFactory = new DefaultTokenizerFactory
    t.setTokenPreProcessor(new CommonPreprocessor)
    val vec = new Word2Vec.Builder()
      .minWordFrequency(5)
      .iterations(1)
      .layerSize(100)
      .seed(42)
      .windowSize(5)
      .iterate(iter)
      .tokenizerFactory(t)
      .build()

    vec.fit()
    val vectors = new ParagraphVectors.Builder()
      .useExistingWordVectors(vec)
      .build()
    // we have to define tokenizer here, because restored model has no idea about it

    val inferredVectorA = vectors.inferVector("This is my world .")
    //    WordVectorSerializer.writeWordVectors(vec, s"${corpus}_vec")
    //    WordVectorSerializer.writeFullModel(vec, s"${corpus}_model")
    //    val lst3 = vec.wordsNearest("man", 10)
    //    println("nearest: " + lst3)
    println("This is my world: " + inferredVectorA.toString)

  }

  def search2() = {
    //    hc.init()
    //    var i = 0
    //    var products = Array[ProductSearch]()
    //    scala.io.Source.fromFile("data\\product_version\\part-00000").getLines().foreach(product => {
    //      logger.info(s"processing product $product")
    //      val res = hc.crawlPage(hc.getBingRes(product), 5)
    //      val pro = ProductSearch(product, res)
    //      products = products :+ pro
    //      i = i + 1
    //      println("current index: " + i)
    //      nd.saveSearchRes(pro)
    //      products = Array[ProductSearch]()
    //      hc.close()
    //
    //    })
    //    nd.saveSearchRes(products)
    //    hc.close()
  }

  def weka() = {
    //    val p3 = DataSource.read("D:\\Program Files\\Weka-3-8\\data\\player3.arff")
    //    p3.setClassIndex(4)
    //    val x = new LinearRegression
    //    x.buildClassifier(p3)
    //    val res = WekaUtils.testModel(x, "D:\\Program Files\\Weka-3-8\\data\\player4.arff", 4)
    //    println(res.toString)
    //    SerializationHelper.write("data\\weka\\lr1.md", x)
    //    val lr1 = SerializationHelper.read("data\\weka\\lr1.md").asInstanceOf[Classifier]
    //    val res2 = WekaUtils.testModel(lr1, "D:\\Program Files\\Weka-3-8\\data\\player4.arff", 4)
    //    println(res2.toString)
  }

  def crossValidate() = {
    //    val folds = 10
    //    val rand = new Random(1)
    //    val randData = new Instances(trainData)
    //    randData.randomize(rand)
    //    randData.stratify(folds)
    //    Range(0, folds).foreach(i => {
    //      val train = randData.trainCV(folds, i)
    //      train.setClassIndex(classIndex)
    //      val test = randData.testCV(folds, i)
    //      trainData.setClassIndex(classIndex)
    //      val eval = new Evaluation(train)
    //      cls.buildClassifier(train)
    //      eval.evaluateModel(cls, test)
    //      SerializationHelper.write(s"E:\\secdata\\wekaData\\evaluation\\eval_$i", eval)
    //      logger.info(eval.toSummaryString())
    //    })
  }

}
