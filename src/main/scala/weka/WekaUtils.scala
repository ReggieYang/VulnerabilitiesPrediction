package weka

import weka.classifiers.AbstractClassifier
import weka.core.converters.ConverterUtils.DataSource

/**
  * Created by ReggieYang on 2017/3/9.
  */
object WekaUtils {

  def testModel(model: AbstractClassifier, testFile: String, classIndex: Int) = {
    val testSet = DataSource.read(testFile)
    testSet.setClassIndex(classIndex)
    Range(0, testSet.size()).foreach(index => testSet.get(index).setClassValue(model.classifyInstance(testSet.get(index))))
    testSet
  }

}
