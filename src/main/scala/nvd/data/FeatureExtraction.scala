package nvd.data

import java.sql.Connection

import util.Utils._

/**
  * Created by ReggieYang on 2016/10/23.
  */
class FeatureExtraction(conn: Connection) {


  def featureByCwe() = {
    val sql = "select cwe, GROUP_CONCAT(summary Separator'\\t') as summary from vulnerability_copy v where v.summary not like \"\\*\\* REJECT \\*\\*%\" group by v.cwe"
    val stmt = conn.createStatement()
    val rs = stmt.executeQuery(sql)
    while (rs.next()) {
      val pathPrefix = "data\\featureByCwe\\"
      val cwe = if (rs.getString("cwe").isEmpty) "cwe-other" else rs.getString("cwe")
      val content = rs.getString("summary")
      writeFile(pathPrefix + cwe, content)
    }
  }

  def featureByImpactScore() = {
    val begin = "SELECT GROUP_CONCAT(summary Separator'\\t') as summary from vulnerability_copy v where v.impact_score >= "
    val middle = " and v.impact_score < "

    val scoreRange = Range(1, 11)
    scoreRange.foreach(score => {
      val sql = begin + score + middle + (score + 1)
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery(sql)
      while (rs.next()) {
        val pathPrefix = "data\\featureByImpactScore\\"
        val fileName = "Impact Score-" + score
        val content = rs.getString("summary")
        writeFile(pathPrefix + fileName, content)
      }
    })


  }

}
