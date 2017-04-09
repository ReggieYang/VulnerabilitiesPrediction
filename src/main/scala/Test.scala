import java.io._
import java.sql.Connection
import java.util
import java.util.Random
import java.util.logging.Level

import com.gargoylesoftware.htmlunit.{IncorrectnessListener, WebClient}
import com.gargoylesoftware.htmlunit.html._
import crawler.HtmlCrawler
import lucene.{FeatureExtraction, LuceneUtils}
import nvd.data.{DBConnection, NvdItemDao, RawDataProcess, SummaryExtraction}
import nvd.model.{ProductSearch, SearchRes, SearchRes2}
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
import org.deeplearning4j.util.ModelSerializer
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory

import collection.JavaConverters._
import w2v.{W2VRNN, Word2VecUtils}
import weka.classifiers.`lazy`.IBk
import weka.classifiers.bayes.{NaiveBayes, NaiveBayesMultinomial}
import weka.{ModelTrain, WekaUtils}
import weka.classifiers.{Classifier, Evaluation}
import weka.classifiers.functions._
import weka.classifiers.meta.{AdaBoostM1, LogitBoost, MultiClassClassifier, Stacking}
import weka.classifiers.rules.{JRip, OneR, PART}
import weka.classifiers.trees._
import weka.core.{Attribute, Instances, SerializationHelper}
import weka.core.converters.ConverterUtils.DataSource
import weka.filters.unsupervised.attribute.StringToNominal
import workflow.MainWorkFlow


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

  lazy val featureWords = Array("attacker", "arbitrary", "vulnerability", "cve", "unspecified", "denial", "crafted", "cross")

  def main(args: Array[String]) = {
    val conn = DBConnection.getConnection
    //    Array("impact", "amount", "category").foreach(WekaUtils.genArff(conn, _))
    //    val rdp = new RawDataProcess
    //    val data = rdp.getSearchSite2(conn)
    //    rdp.writeDesctoFile(conn)
    //    val se = new SummaryExtraction(conn)
    //    se.writeVectors("search_res3")

    val mw = new MainWorkFlow(conn)
    mw.run()

    conn.close()

    //    search2(data)
    //    val mt = new ModelTrain

    //    val words = LuceneUtils.getWordsFrequencyFromFile("data\\summary\\part-00000")
    //
    //    logger.info(words.mkString("\n"))
    //    val wordsRDD = sc.parallelize(words).repartition(1).sortBy(0 - _._2)
    //    wordsRDD.saveAsTextFile("data\\summaryWordCount")
    //    mt.preprocess("E:\\secdata\\wekaData\\train\\impact.arff", new StringToNominal)

    //    val mlpAmount = SerializationHelper.read("E:\\secdata\\wekaData\\model\\cv_amount_mpp").asInstanceOf[MultilayerPerceptron]
    //    //    val mlpAmount = SerializationHelper.read("E:\\secdata\\wekaData\\model\\amount_mlp.cls").asInstanceOf[MultilayerPerceptron]
    //    val testData2 = DataSource.read("E:\\secdata\\wekaData\\train\\amount_train.arff")
    //    testData2.setClassIndex(testData2.numAttributes() - 1)
    //    logger.info("CRE: " + WekaUtils.calCRE(mlpAmount, testData2))
    //
    //    val mlpImpact = SerializationHelper.read("E:\\secdata\\wekaData\\model\\impact_MultilayerPerceptron.cls").asInstanceOf[MultilayerPerceptron]
    //    val testData = DataSource.read("E:\\secdata\\wekaData\\train\\impact_MultilayerPerceptron.arff")
    //    testData.setClassIndex(testData.numAttributes() - 1)
    //
    //    val mccCategory = SerializationHelper.read("E:\\secdata\\wekaData\\model\\category_mcc.cls").asInstanceOf[MultiClassClassifier]
    //    val testData3 = DataSource.read("E:\\secdata\\wekaData\\train\\category_classification.arff")
    //    testData3.setClassIndex(testData3.numAttributes() - 1)
    //    logger.info("FRank:" + WekaUtils.calFRank(mccCategory, testData3))

    //    Range(0, testData2.size()).foreach(index => {
    //      val pred = mlpAmount.classifyInstance(testData2.get(index))
    //      val actual = testData2.get(index).classValue()
    //      if (actual > 100d)
    //        println(s"Actual: $actual\tPredicted: $pred")
    //    })

    //    logger.info("FRank: " + WekaUtils.calFRank(mccCategory, testData3))
    //    logger.info("Corrected Relative Error: " + WekaUtils.calCRE(mlpImpact, testData))
    //    logger.info("Relative Error: " + calCRE(mlpAmount, testData2))

    //    val eval = new Evaluation(testData)
    //    eval.evaluateModel(mlpAmount, testData)
    //    logger.info(eval.toSummaryString())
    //    mt.crossValidate("amount", new RandomForest(), "regression")


    //    W2VRNN.trainModel("D:\\Documents\\glove.6B\\glove.6B.100d.txt", "D:\\Documents\\Downloads\\aclImdb_v2")

    //    W2VRNN.makeParVec()
    //    W2VRNN.parInfer()

    //    val trainData = DataSource.read("E:\\secdata\\wekaData\\train\\category_nominal.arff")
    //
    //    val cls = new MultiClassClassifier
    //    val folds = 10
    //    val rand = new Random(1)
    //    trainData.setClassIndex(trainData.numAttributes() - 1)
    //    val randData = new Instances(trainData)
    //    logger.info("random seed: " + rand)
    //    randData.randomize(rand)
    //    randData.stratify(folds)
    //    Range(0, 1).foreach(i => {
    //      val train = randData.trainCV(folds, i)
    //      val test = randData.testCV(folds, i)
    //      val eval = new Evaluation(train)
    //      cls.buildClassifier(train)
    //      SerializationHelper.write(s"E:\\secdata\\wekaData\\model\\cv_category_mlc", cls)
    //      eval.evaluateModel(cls, test)
    //      SerializationHelper.write(s"E:\\secdata\\wekaData\\evaluation\\eval_category_mlc", eval)
    //      logger.info(eval.toSummaryString())
    //      logger.info("CRE: " + WekaUtils.calFRank(cls, test))
    //    })


  }


  def search() = {

    //    val rdd2: RDD[String] = sc.parallelize(p)
    //    rdd2.repartition(1).saveAsTextFile("data\\product_version")

    val productRDD = sc.textFile("data\\vulAmount\\part-00000")

    productRDD.foreachPartition(it => {
      val conn = DBConnection.getConnection
      val hc = new HtmlCrawler
      val nd = new NvdItemDao(conn)
      hc.init()
      var i = 0
      var tempPs = Array[ProductSearch]()
      it.filter(p => p.split("\t")(1).toInt > 1).foreach(productWithAmount => {
        val product = productWithAmount.split("\t")(0)

        i = i + 1
        println(s"processing product: $product")

        val res = featureWords.flatMap(fw => {
          val kw = s"$product+$fw"
          val bingRes = hc.getBingRes(kw)
          println("product: " + kw + " bingRes: " + bingRes.mkString(","))
          bingRes.map(search => ProductSearch(kw, Array(search)))
        })

        //          hc.getBingRes(product).map(search => ProductSearch(product, Array(search)))

        //        val ps = ProductSearch(product, hc.crawlPage(hc.getBingRes(product)))
        //        tempPs = tempPs :+ ps
        tempPs = tempPs ++ res

        //        if (i % 1 == 0) {
        nd.saveSearchRes(res)
        //          tempPs = Array[ProductSearch]()
        //        }
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

  def search2(data: Array[Array[String]]) = {
    val siteRDD = sc.parallelize(data)

    siteRDD.foreachPartition(it => {
      val conn = DBConnection.getConnection
      val hc = new HtmlCrawler
      val nd = new NvdItemDao(conn)
      hc.init()
      var i = 0
      //      var tempPs = Array[SearchRes]()
      it.foreach(siteWithId => {
        val id = siteWithId(0)
        val site = siteWithId(1)
        i = i + 1
        println(s"processing site: $site")

        val res = hc.crawlPage(site)
        val sr = SearchRes2(id.toInt, site, res)
        nd.saveSearchSiteRes(sr)
        //        tempPs = tempPs :+ sr
        //
        //        if (i % 2 == 0) {
        //          nd.saveSearchSiteRes(tempPs)
        //          tempPs = Array[SearchRes]()
        //        }
      })
      //      nd.saveSearchSiteRes(tempPs)
      conn.close()
    })
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
    //        val folds = 10
    //        val rand = new Random(1)
    //        val randData = new Instances(trainData)
    //        randData.randomize(rand)
    //        randData.stratify(folds)
    //        Range(0, folds).foreach(i => {
    //          val train = randData.trainCV(folds, i)
    //          train.setClassIndex(classIndex)
    //          val test = randData.testCV(folds, i)
    //          trainData.setClassIndex(classIndex)
    //          val eval = new Evaluation(train)
    //          cls.buildClassifier(train)
    //          eval.evaluateModel(cls, test)
    //          SerializationHelper.write(s"E:\\secdata\\wekaData\\evaluation\\eval_$i", eval)
    //          logger.info(eval.toSummaryString())
    //        })
  }


}
