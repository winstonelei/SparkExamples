package main.scala.study.language
import java.util

import scala.io.Source

/**
  * Created by Stone on 2016/8/25.
  */
object FunctionOps {
  def main(args: Array[String]) {

  /*val width = args(0).toInt
  for (arg <- args.drop(1))
    processData(arg, width)
*/

  var increase = (x: Int) => x + 1
  println(increase(10))
  increase = (x: Int) => x + 9999

  val someNumbers = List(-11, -10, -5, 0, 5, 10)
  someNumbers.foreach((x: Int) => print (x))
  println

  someNumbers.filter((x: Int) => x > 0).foreach((x: Int) => print (x))
  println
  someNumbers.filter((x) => x > 0).foreach((x: Int) => print (x))
  println
  someNumbers.filter(x => x > 0).foreach((x: Int) => print (x))
  println
  someNumbers.filter(_ > 0).foreach((x: Int) => print (x))
  println
  val f = (_: Int) + (_: Int)
  println(f(5, 10))
}


def processData(filename: String, width: Int) {

  def processLine(line: String) {
    if (line.length > width)
      println(filename + ": " + line)
  }

  val source = Source.fromFile(filename)
  for (line <- source.getLines)
    processLine(line)
}

}