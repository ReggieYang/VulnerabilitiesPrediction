package lucene

import java.io._
import util.Utils._

/**
  * Created by ReggieYang on 2016/10/24.
  */
class FeatureExtraction {

  def summaryToFrequency(filePath: String) = {
    val summary = scala.io.Source.fromFile(new File(filePath)).getLines().toArray.mkString(" ")
    val wf = LuceneUtils.getWordsFrequency(summary)

    val suffix = "_wf"
    val bw = new BufferedWriter(new FileWriter(new File(filePath + suffix)))
    wf.foreach(x => {
      bw.write(x._1 + TabSep + x._2)
      bw.newLine()
    })
    bw.close()

  }

}
