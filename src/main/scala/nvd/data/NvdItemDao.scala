package nvd.data

import java.sql.Connection

import nvd.model.NvdItem
import util.Utils

/**
  * Created by ReggieYang on 2016/10/22.
  */
class NvdItemDao(conn: Connection) {


  def saveFeature(items: Array[NvdItem]) = {
    conn.setAutoCommit(false)
    val cmd = conn.prepareStatement("insert into feature(id, reference, summary) values(?,?,?)")

//    var i = 0
    items.foreach(item => {
      cmd.setString(1, item.id)
      cmd.setString(2, item.reference)
      cmd.setString(3, item.summary)
      cmd.addBatch()
//      i = i + 1
//      if (i % 1000 == 0) {
//        conn.commit()
//        cmd.executeBatch()
//      }
    })

    cmd.executeBatch()
    conn.commit()
    cmd.close()

  }

  def saveItem(items: Array[NvdItem]) = {
    conn.setAutoCommit(false)
    val cmd = conn.prepareStatement("insert into vulnerability(id, product, impact_score, cwe) values(?,?,?,?)")

//    var i = 0
    items.foreach(item => {
      cmd.setString(1, item.id)
//      println(item.id)
//      println(item.product)
      cmd.setString(2, item.product)
      cmd.setDouble(3, item.impactScore)
      cmd.setString(4, item.cwe)
      cmd.addBatch()
//      i = i + 1
//      if (i % 1000 == 0) {
//        conn.commit()
//        cmd.executeBatch()
//      }
    })


    conn.commit()
    cmd.executeBatch()
    cmd.close()

  }

}
