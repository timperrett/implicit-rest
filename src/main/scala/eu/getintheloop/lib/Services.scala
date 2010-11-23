package eu.getintheloop.lib

import net.liftweb.http.{LiftResponse,XmlResponse,PlainTextResponse}
import net.liftweb.http.rest.RestHelper

trait ReturnAs[A, B] {
  def as(a:A):B
}
object ReturnAs{
  implicit def f2ReturnAs[A, B](f:A => B):ReturnAs[A, B] = 
    new ReturnAs[A, B]{
      def as(a:A) = f(a)
    }
}

trait Return[A] {
  type As[B] = ReturnAs[A, B]
}
object Return {
  def apply[A, B](a:A)(implicit f: ReturnAs[A, B]) = f.as(a)
}


case class Book(publisher: String, title: String)
object Book {
  implicit val booksAsXml: Return[List[Book]]#As[XmlResponse] = 
    (books:List[Book]) => 
      XmlResponse(<books>{books.flatMap{b => <book publisher={b.publisher} title={b.title}/>}}</books>)
  
  implicit val booksAsPlainText: ReturnAs[List[Book], PlainTextResponse] = 
    (books:List[Book]) => 
      PlainTextResponse("Books\n"+books.map(b => "publisher:"+b.publisher + ",title:"+b.title))
}

object Bookshop {
  val stock = List(
    Book("Bloomsbury", "Harry Potter and the Deathly Hallows"),
    Book("Bloomsbury", "Harry Potter and the Goblet of Fire"),
    Book("Manning", "Scala in Depth"),
    Book("Manning", "Lift in Action")
  )
}

trait BookshopService {
  def list[R : Return[List[Book]]#As]:R =
    Return(Bookshop.stock)

  def listByPublisher[R : Return[List[Book]]#As](publisher:String):R =
    Return(Bookshop.stock.filter(_.publisher equalsIgnoreCase publisher))
}

object BookshopHttpService extends BookshopService with RestHelper {
  serve {
    // xml services
    case "bookshop" :: "books" :: Nil XmlGet _ => list[XmlResponse]
    case "bookshop" :: "books" :: publisher :: Nil XmlGet _ => listByPublisher[XmlResponse](publisher)
    // plain text services
    case "bookshop" :: "books" :: Nil Get _ => list[PlainTextResponse]
    case "bookshop" :: "books" :: publisher :: Nil Get _ => listByPublisher[PlainTextResponse](publisher)
  }
}

