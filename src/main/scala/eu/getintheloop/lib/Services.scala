/******************************************************************
          DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
                  Version 2, December 2004 

        Copyright (C) 2004 Sam Hocevar <sam@hocevar.net> 

Everyone is permitted to copy and distribute verbatim or modified 
copies of this license document, and changing it is allowed as long 
as the name is changed. 

          DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

          0. You just DO WHAT THE FUCK YOU WANT TO.

******************************************************************/

package eu.getintheloop.lib

import net.liftweb.http.{LiftResponse,XmlResponse,PlainTextResponse}
import net.liftweb.http.rest.RestHelper


/*
STEP 1: Define your domain classes
*/
case class Book(publisher: String, title: String)

object Bookshop {
  val stock = List(
    Book("Bloomsbury", "Harry Potter and the Deathly Hallows"),
    Book("Bloomsbury", "Harry Potter and the Goblet of Fire"),
    Book("Manning", "Scala in Depth"),
    Book("Manning", "Lift in Action")
  )
}

/*
STEP 2: Define some service helpers and implementation
        magic to handle the service definition
*/
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
  def apply[A, B](a: A)(implicit f: ReturnAs[A, B]) = f.as(a)
}

/*
STEP 3: Define your implicit conversions for A => B :> LiftResponse media types
*/
object Book {
  implicit val booksAsXml: Return[List[Book]]#As[XmlResponse] = 
    (books:List[Book]) => 
      XmlResponse(<books>{books.flatMap{b => <book publisher={b.publisher} title={b.title}/>}}</books>)
  
  implicit val booksAsPlainText: ReturnAs[List[Book], PlainTextResponse] = 
    (books:List[Book]) => 
      PlainTextResponse("Books\n"+books.map(b => "publisher:"+b.publisher + ",title:"+b.title))
}


/*
STEP 4: Define the services themselves
*/
trait BookshopService {
  def list[R : Return[List[Book]]#As]:R =
    Return(Bookshop.stock)

  def listByPublisher[R : Return[List[Book]]#As](publisher:String):R =
    Return(Bookshop.stock.filter(_.publisher equalsIgnoreCase publisher))
}

/*
STEP 5: Impement the services. Dont forget to write this up in Boot.scala.
*/
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

