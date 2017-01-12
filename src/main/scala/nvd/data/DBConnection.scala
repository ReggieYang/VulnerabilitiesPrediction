package nvd.data

import java.sql.{Connection, DriverManager, SQLException}

/**
  * Created by ReggieYang on 2016/10/22.
  */
object DBConnection {

  var conn: Connection = null

  def getConnection: Connection = {
    //调用Class.forName()方法加载驱动程序
    Class.forName("com.mysql.jdbc.Driver")
    val url = "jdbc:mysql://localhost:3306/nvd"
    conn = DriverManager.getConnection(url, "root", "root")
    conn
  }

  def closeConnection = {
    conn.close()
  }



}
