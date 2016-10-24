package lucene

import java.io.StringReader

import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

/**
  * Created by ReggieYang on 2016/10/24.
  */
object LuceneUtils {

  def getWords(content:String):Array[String] = {
    val analyzer = new StandardAnalyzer()
    val sr = new StringReader(content)
    val ts = analyzer.tokenStream("word", sr)
    val tsI = new TSIterator(ts)
    val words = tsI.toArray
    ts.close()
    words
  }

  def getWordsFrequency(content:String):Map[String, Int] = {
    countFrequency(getWords(content))
  }

  def countFrequency(words: Array[String]): Map[String, Int] = {
    val tempMap = scala.collection.mutable.Map[String, Int]()
    words.foreach(word => {
      tempMap.get(word) match {
        case Some(count) => tempMap.put(word, count + 1)
        case _ => tempMap.put(word, 1)
      }
    })
    tempMap.toMap
  }

  class TSIterator(ts: TokenStream) extends Iterator[String] {
    ts.reset()
    ts.addAttribute(classOf[CharTermAttribute])

    override def next(): String = {
      ts.getAttribute(classOf[CharTermAttribute]).toString
    }

    override def hasNext: Boolean = ts.incrementToken()
  }
}


