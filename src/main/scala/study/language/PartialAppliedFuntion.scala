package main.scala.study.language

/**
  * Created by dell on 2016/8/25.
  */
object PartialAppliedFuntion {

  def increment(list: List[Int]): List[Int] = list match {
    case List() => List()
    case head :: tail => head + 1 :: increment(tail)
  }

  def increment_MoreEffective(list: List[Int]): List[Int] = {
    var result = List[Int]()
    //for(element <- list) result = result ::: List(element +1)
    for(element <- list){
       result = result ::: List(element + 1)
    }
    result
  }

  def increment_MostEffective(list: List[Int]): List[Int] = {
    import scala.collection.mutable.ListBuffer
    var buffer = new ListBuffer[Int]
    //for(element <- list) buffer += element + 1
    for(element <- list){
        //buffer += element + 1
         buffer.+=(element+1)
    }
    buffer.toList
  }




  def main(args: Array[String]){

    val list1 = List(1,2,3,4,5,6,7,8,9)
    println(increment(list1))

    println(increment_MoreEffective(list1))
    println(increment_MostEffective(list1))

   println("----------------------------------")


   /* val data = List(1, 2, 3, 4, 5, 6)
    data.foreach(print _)
    data.foreach(x => print(x))
*/
    def sum(a:Int,b:Int,c:Int) = a+b+c

    val fp_a = sum _

    println(fp_a(1,2,5))
    println(fp_a.apply(1, 2, 3))

    val list : List[Int] = List(1,2,3,4,5)

    println(list.isEmpty)
    println(list.head)
    println(list.tail)
    println(list.length)
    println(list.drop(2))
    println(list.map(_*2))




    /*
      def sum(a: Int, b: Int, c: Int) = a + b + c
      println(sum(1, 2, 3))

      val fp_a = sum _
      println(fp_a(1, 2, 3))
      println(fp_a.apply(1, 2, 3))
      val fp_b = sum(1, _: Int, 3)
      println(fp_b(2))
      println(fp_b(10))

      data.foreach(println(_))
      data.foreach(println)*/
/*   */
  }

}
