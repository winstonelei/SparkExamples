package main.scala.study.language

/**
  * Created by dell on 2016/8/25.
  */
case class Book(title : String , authors : List[String])

case class Pager(titile:String,pages:Int)


object  For_Query {
  def main(args: Array[String]) {

    val books: List[Book] = List(
      Book("Structure and Interpretation ", List("Abelson , Harold", "Sussman")),
      Book("Principles of Compiler Design", List("Aho, Alfred", "Ullman, Jeffrey")),
      Book("Programming in Modula-2", List("Wirth, Niklaus")),
      Book("Introduction to Functional Programming", List("Gosling, Richard")),
      Book("The Java Language Specification", List("Gosling, James", "Joy, Bill", "Steele, Guy", "Bracha, Gilad")))

    val result = for(b <- books ; a <- b.authors if a startsWith "Gosling") yield b.title
    val result1 = for(b <- books if (b.title indexOf "Programming") >= 0 ) yield b.title
    println(result)
    println(result1)

    var seqPager =Seq(Pager("lisi",12),Pager("zhangsan",100),Pager("wangwu",24))


    println(seqPager.maxBy(pager => pager.pages))

    println("------------------ ")

    println(seqPager.minBy(pager => pager.pages))


  }


}
