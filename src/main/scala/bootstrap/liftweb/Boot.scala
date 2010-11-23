package bootstrap.liftweb

import net.liftweb.http.LiftRules
import eu.getintheloop.lib.BookshopHttpService

class Boot {
  def boot {
    LiftRules.addToPackages("eu.getintheloop")

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    
    LiftRules.statelessDispatchTable.append(BookshopHttpService)
  }
}