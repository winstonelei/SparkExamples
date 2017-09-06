package study.language

import java.io.File

import scala.io.Source


/**
  * Created by winstone on 2017/7/27.
  */
object Context_Helper {

  implicit class  FileEnhancer(file : File){
    def read = Source.fromFile(file.getPath).mkString
  }

  implicit  class Op(x:Int){
    def addSap(second:Int)=x+second
  }


}

object ImplictClass{

  def main(args: Array[String]) {

    import Context_Helper._

    println(new File("F:\\tmp\\b.txt").read)

    println(1.addSap(2))

  }

}
