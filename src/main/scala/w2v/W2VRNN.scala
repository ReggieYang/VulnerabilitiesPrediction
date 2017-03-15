package w2v

import java.io.File

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import org.deeplearning4j.nn.conf.GradientNormalization
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.Updater
import org.deeplearning4j.nn.conf.layers.GravesLSTM
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.text.documentiterator.FileLabelAwareIterator
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.slf4j.LoggerFactory
import org.datavec.api.util.ClassPathResource
import org.deeplearning4j.berkeley.Pair
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors
import org.deeplearning4j.models.word2vec.VocabWord
import org.deeplearning4j.text.documentiterator.FileLabelAwareIterator
import org.deeplearning4j.text.documentiterator.LabelAwareIterator
import org.deeplearning4j.text.documentiterator.LabelledDocument
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.ops.transforms.Transforms
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
  * Created by ReggieYang on 2017/3/9.
  */
object W2VRNN {

  lazy val logger = LoggerFactory.getLogger(this.getClass)

  def trainModel(word2VecPath: String, dataPath: String) = {
    val batchSize = 64 //Number of examples in each minibatch
    val vectorSize = 100 //Size of the word vectors. 300 in the Google News model
    val nEpochs = 1 //Number of epochs (full passes of training data) to train on
    val truncateReviewsToLength = 256 //Truncate reviews with length (# words) greater than this

    val conf = new NeuralNetConfiguration.Builder()
      .updater(Updater.ADAM).adamMeanDecay(0.9).adamVarDecay(0.999)
      .regularization(true).l2(1e-5)
      .weightInit(WeightInit.XAVIER)
      .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
      .learningRate(2e-2)
      .list()
      .layer(0, new GravesLSTM.Builder().nIn(vectorSize).nOut(256)
        .activation(Activation.TANH).build())
      .layer(1, new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
        .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(256).nOut(2).build())
      .pretrain(false).backprop(true).build()

    val net = new MultiLayerNetwork(conf)
    net.init()
    net.setListeners(new ScoreIterationListener(1))

    val wordVectors = WordVectorSerializer.loadStaticModel(new File(word2VecPath))
    val train = new SentimentExampleIterator(dataPath, wordVectors, batchSize, truncateReviewsToLength, true)
    val test = new SentimentExampleIterator(dataPath, wordVectors, batchSize, truncateReviewsToLength, false)

    logger.info("Starting training")

    //    Range(0, nEpochs).foreach(i => {
    //      net.fit(train)
    //      train.reset()
    //      logger.info(s"Epoch $i complete. Starting evaluation: ")
    //
    //      ModelSerializer.writeModel(net, "D:\\workspace\\VulnerabilitiesPrediction\\data\\model1.md", true)
    //            val evaluation = new Evaluation()
    //            while (test.hasNext) {
    //              val t = test.next()
    //              val features = t.getFeatureMatrix
    //              val lables = t.getLabels
    //              val inMask = t.getFeaturesMaskArray
    //              val outMask = t.getLabelsMaskArray
    //              val predicted = net.output(features, false, inMask, outMask)
    //
    //              evaluation.evalTimeSeries(lables, predicted, outMask)
    //            }

    //      test.reset()
    //      logger.info(evaluation.stats())

    //      val firstPositiveReviewFile = new File(FilenameUtils.concat(dataPath, "aclImdb/test/pos/0_10.txt"))
    //      val firstPositiveReview = FileUtils.readFileToString(firstPositiveReviewFile)
    //
    val features = test.loadFeaturesFromFile(new File("D:\\Documents\\Downloads\\aclImdb_v1\\aclImdb\\test\\neg\\10_3.txt"), truncateReviewsToLength)
    logger.info("feature: " + features)
    //      val networkOutput = net.output(features)
    //      val timeSeriesLength = networkOutput.size(2)
    //      val probabilitiesAtLastWord = networkOutput.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength - 1))
    //
    //
    //      logger.info("\n\n-------------------------------")
    //      logger.info("First positive review: \n" + firstPositiveReview)
    //      logger.info("\n\nProbabilities at last time step:")
    //      logger.info("p(positive): " + probabilitiesAtLastWord.getDouble(0))
    //      logger.info("p(negative): " + probabilitiesAtLastWord.getDouble(1))
    //
    //      logger.info("----- Example complete -----")
    //    })

  }

  def t2v(word2VecPath: String, dataPath: String) = {
    val batchSize = 64 //Number of examples in each minibatch
    val vectorSize = 100 //Size of the word vectors. 300 in the Google News model
    val nEpochs = 1 //Number of epochs (full passes of training data) to train on
    val truncateReviewsToLength = 256 //Truncate reviews with length (# words) greater than this

    val conf = new NeuralNetConfiguration.Builder()
      .updater(Updater.ADAM).adamMeanDecay(0.9).adamVarDecay(0.999)
      .regularization(true).l2(1e-5)
      .weightInit(WeightInit.XAVIER)
      .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
      .learningRate(2e-2)
      .list()
      .layer(0, new GravesLSTM.Builder().nIn(vectorSize).nOut(256)
        .activation(Activation.TANH).build())
      //      .layer(1, new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
      //        .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(256).nOut(2).build())
      .pretrain(false).backprop(true).build()


    val net = new MultiLayerNetwork(conf)
    net.init()
    net.setListeners(new ScoreIterationListener(1))

    val wordVectors = WordVectorSerializer.loadStaticModel(new File(word2VecPath))
    val train = new SentimentExampleIterator(dataPath, wordVectors, batchSize, truncateReviewsToLength, true)
    val test = new SentimentExampleIterator(dataPath, wordVectors, batchSize, truncateReviewsToLength, false)

    logger.info("Starting training")

    Range(0, nEpochs).foreach(i => {
      net.fit(train)
      train.reset()
      logger.info(s"Epoch $i complete. Starting evaluation: ")

      val evaluation = new Evaluation()
      while (test.hasNext) {
        val t = test.next()
        val features = t.getFeatureMatrix
        val lables = t.getLabels
        val inMask = t.getFeaturesMaskArray
        val outMask = t.getLabelsMaskArray
        val predicted = net.output(features, false, inMask, outMask)

        evaluation.evalTimeSeries(lables, predicted, outMask)
      }

      ModelSerializer.writeModel(net, "D:\\workspace\\VulnerabilitiesPrediction\\data\\model1.md", true)


      logger.info(evaluation.stats())

      val firstPositiveReviewFile = new File(FilenameUtils.concat(dataPath, "aclImdb/test/pos/0_10.txt"))
      val firstPositiveReview = FileUtils.readFileToString(firstPositiveReviewFile)

      val features = test.loadFeaturesFromString(firstPositiveReview, truncateReviewsToLength)
      val networkOutput = net.output(features)
      val timeSeriesLength = networkOutput.size(2)
      val probabilitiesAtLastWord = networkOutput.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength - 1))

      logger.info("\n\n-------------------------------")
      logger.info("First positive review: \n" + firstPositiveReview)
      logger.info("\n\nProbabilities at last time step:")
      logger.info("p(positive): " + probabilitiesAtLastWord.getDouble(0))
      logger.info("p(negative): " + probabilitiesAtLastWord.getDouble(1))

      logger.info("----- Example complete -----")
    })

  }

  def makeParVec() = {
    //    val resource = new ClassPathResource("D:\\Documents\\Downloads\\dl4j-examples-master\\dl4j-examples\\src\\main\\resources\\paravec\\labeled")

    // build a iterator for our dataset
    val iterator = new FileLabelAwareIterator.Builder()
      .addSourceFolder(new File("D:\\Documents\\Downloads\\dl4j-examples-master\\dl4j-examples\\src\\main\\resources\\paravec\\labeled"))
      .build()

    val tokenizerFactory = new DefaultTokenizerFactory()
    tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor())

    // ParagraphVectors training configuration
    val paragraphVectors = new ParagraphVectors.Builder()
      .learningRate(0.025)
      .minLearningRate(0.001)
      .batchSize(1000)
      .epochs(20)
      .iterate(iterator)
      .trainWordVectors(true)
      .tokenizerFactory(tokenizerFactory)
      .build()

    // Start model training
    paragraphVectors.fit()
    WordVectorSerializer.writeParagraphVectors(paragraphVectors, "data\\pv.pv")

    logger.info("vector of time: " + paragraphVectors.inferVector("time time"))
  }

  def parInfer() = {
    val pvPath: String = "D:\\workspace\\VulnerabilitiesPrediction\\data\\word2vec\\summary.pv"

    //        WordVectorSerializer.writeParagraphVectors(vec, pvPath);
    val t: TokenizerFactory = new DefaultTokenizerFactory
    t.setTokenPreProcessor(new CommonPreprocessor)

    // we load externally originated model
    val vec: ParagraphVectors = WordVectorSerializer.readParagraphVectors(pvPath)
    vec.setTokenizerFactory(t)

    val inferredVectorA: INDArray = vec.inferVector("Buffer overflow in NIS+, in Sun's rpc.nisd program .")
    val inferredVectorA2: INDArray = vec.inferVector("root privileges via buffer overflow in command on SGI IRIX systems .")
    val inferredVectorB: INDArray = vec.inferVector("root privileges via buffer overflow in eject command on SGI IRIX systems .")

    logger.info("overflow: " + vec.getWordVector("overflow").mkString(","))

    logger.info("vectorA: " + inferredVectorA)
    logger.info("vectorA2: " + inferredVectorA2)
    logger.info("vectorB: " + inferredVectorB)

    logger.info("Cosine similarity A/B: {}", Transforms.cosineSim(inferredVectorA, inferredVectorB))

    // equality expected here, since inference is happening for the same sentences
    logger.info("Cosine similarity B/A2: {}", Transforms.cosineSim(inferredVectorB, inferredVectorA2))
  }


}
