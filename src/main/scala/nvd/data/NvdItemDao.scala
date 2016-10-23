package nvd.data

import java.sql.Connection

import nvd.model.NvdItem
import util.Utils

/**
  * Created by ReggieYang on 2016/10/22.
  */
class NvdItemDao(conn: Connection) {


  def saveList(items: Array[NvdItem]) = {
    conn.setAutoCommit(false)
    val cmd = conn.prepareStatement("insert into vulnerability_copy(id, product, impact_score, reference, cwe, summary) values(?,?,?,?,?,?)")

    var i = 0
    items.foreach(item => {
      cmd.setString(1, item.id)
      println(item.id)
      println(item.product)
      cmd.setString(2, if (item.product.length > 6000) Utils.EmptyString else item.product)
      cmd.setDouble(3, item.impactScore)
      cmd.setString(4, item.reference)
      cmd.setString(5, item.cwe)
      cmd.setString(6, item.summary)
      cmd.addBatch()
      i = i + 1
      if (i % 1000 == 0) {
        conn.commit()
        cmd.executeBatch()
      }
    })

    conn.commit()
    cmd.executeBatch()
    cmd.close()

    //    val sql = "insert into vulnerability(id, product, impact_score, reference, cwe, summary) value " + item.toSqlItem
    //    val stmt = conn.prepareStatement(sql)
    //    stmt.execute()
    //    stmt.close()
  }

}
