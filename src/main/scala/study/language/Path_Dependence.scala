package main.scala.study.language

/**
  * Created by dell on 2016/8/25.
  */
class Outer {

  private val x = 10

  class Inner {
    private val y = x + 10
    def getVaribleVal(): Unit ={
      println(y)
      println(x)
    }
  }
}


object Path_Dependence {
  def main(args: Array[String]){
    val outer = new Outer
    val inner =  new outer.Inner
    val inner2: outer.Inner = new outer.Inner
    //inner.getVaribleVal()
    inner2.getVaribleVal()

   /* val o1 = new Outer
    val o2 = new Outer
    val i: Outer#Inner = new o1.Inner
*/

  }
}
