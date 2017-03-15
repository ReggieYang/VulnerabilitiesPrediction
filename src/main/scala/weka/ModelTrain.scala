package weka

import java.io.{FileOutputStream, ObjectOutputStream}

import org.slf4j.LoggerFactory
import weka.classifiers.Classifier
import weka.core.Instances
import weka.core.converters.ConverterUtils.{DataSink, DataSource}
import weka.filters.Filter
import weka.filters.supervised.attribute.Discretize
import weka.filters.unsupervised.attribute.StringToNominal

/**
  * Created by ReggieYang on 2017/3/15.
  */
class ModelTrain {

  lazy val logger = LoggerFactory.getLogger(this.getClass)

  def preprocess(filePath: String, filter: Filter): String = {
    val testSet = DataSource.read(filePath)
    filter.setInputFormat(testSet)
    val filteredFilePath = filePath.replaceAll("(.*\\\\)(.*)(\\.arff)", "$1$2_nominal$3")
    DataSink.write(filteredFilePath, Filter.useFilter(testSet, filter))
    filteredFilePath
  }

  def modelTrain(filePath: String, cls: Classifier, clsPath: String, classIndex: Int) = {
    logger.info("Begin training...")
    val instances = DataSource.read(filePath)
    instances.setClassIndex(classIndex)
    cls.buildClassifier(instances)
    logger.info("Training completed. Begin saving...")
    val oos = new ObjectOutputStream(new FileOutputStream(clsPath))
    oos.writeObject(cls)
    logger.info("Saving completed.")
    oos.flush()
    oos.close()
  }

}
