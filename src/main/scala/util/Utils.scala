package util

import java.io.{BufferedWriter, File, FileWriter}

/**
  * Created by ReggieYang on 2016/10/22.
  */
object Utils {

  lazy val TabSep = "\t"
  lazy val EmptyString = ""

  def writeFile(filePath: String, content:String) = {
    val file = new File(filePath)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(content)
    bw.close()
  }

}
