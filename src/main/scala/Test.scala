import java.io.{BufferedWriter, File, FileOutputStream, FileWriter}
import java.util

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html._
import crawler.HtmlCrawler
import lucene.{FeatureExtraction, LuceneUtils}
import nvd.data.{DBConnection, NvdItemDao, RawDataProcess, SummaryExtraction}
import nvd.model.ProductSearch
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
import weka.WekaUtils
import weka.classifiers.functions.{LinearRegression, SimpleLinearRegression}
import weka.core.Attribute
import weka.core.converters.ConverterUtils.DataSource


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

    //    val folder = new File("data\\word2vec\\enwik_split")
    //    folder.listFiles().filter(file => file.getName.startsWith("part") && !file.getName.endsWith("crc"))
    //      .foreach(file => {
    //        val bw = new BufferedWriter(new FileWriter(new File(file.getPath.replaceAll("enwik_split", "enwik_split_raw"))))
    //        val rawText = Jsoup.parse(file, "gbk").text().replaceAll("\\s+", " ")
    //        bw.write(rawText)
    //        bw.close()
    //      }
    //      )

    //    val rdd = sc.textFile("data\\word2vec\\enwik9")
    //    rdd.repartition(10).saveAsTextFile("data\\word2vec\\enwik_split")

    //    val rawText = Jsoup.parse(new File("data\\word2vec\\SogouT.mini.txt"), "gbk").text().replaceAll("\\s+", " ")
    //    val bw = new BufferedWriter(new FileWriter(new File("data\\word2vec\\raw_text")))
    //    bw.write(rawText)
    //    bw.close()

    //    val p = Array("James", "Reggie")
    //    val p = Range(2002, 2018).flatMap(year => rd.getProductList(s"data\\rawData\\nvdcve-2.0-$year.xml")).toSet.toArray
    //      .map(_.toString)

    //    //        readRawData.readData(conn)
    //    //    //  read raw data
    //    //
    //    val nd = new NvdItemDao(conn)
    //
    //    val se = new SummaryExtraction(conn)
    //
    ////        se.featureByCwe()
    ////        se.featureByImpactScore()
    //    //    se.featureByProductDB(products)
    //    //    se.featureByProduct()
    //
    //    DBConnection.closeConnection
    //
    //    val fe = new FeatureExtraction
    //    fe.summaryToFrequencyDir("data\\test")
    //    val s = new SummaryExtraction(DBConnection.getConnection)
    //    s.crawlReferenceData()
    //
    //
    //        val corpus = "data\\word2vec\\enwik_split\\part-00000"
    //        w2v(corpus)

    val vec = WordVectorSerializer.readWord2VecModel("D:\\Documents\\glove.6B\\glove.6B.100d.txt")
    //    val vec = WordVectorSerializer.loadGoogleModel(new File("E:\\Download\\GoogleNews-vectors-negative300.bin.gz"), true)
    logger.info("man vector: " + vec.getWordVector("man").mkString(","))

    //        val p3 = DataSource.read("D:\\Program Files\\Weka-3-8\\data\\player3.arff")
    //        p3.setClassIndex(4)
    //        val x = new LinearRegression
    //        x.buildClassifier(p3)
    //        val res = WekaUtils.testModel(x, "D:\\Program Files\\Weka-3-8\\data\\player4.arff", 4)
    //        println(res.toString)

    BagOfWordsVectorizer.Builder

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
    //    WordVectorSerializer.writeWordVectors(vec, s"${corpus}_vec")
    WordVectorSerializer.writeFullModel(vec, s"${corpus}_model")
    val lst3 = vec.wordsNearest("man", 10)
    println("nearest: " + lst3)

  }

}
