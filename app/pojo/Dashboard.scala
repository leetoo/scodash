package pojo

object Dashboard {

  trait Cmd

  case class Watch() extends Cmd
  case class UnWatch() extends Cmd
  case class Data(items: Map[String, Item]) extends Cmd
  case class IncrementItem(name: String) extends Cmd
  case class DecrementItem(name: String) extends Cmd
  case class AddItem() extends Cmd
  case class RemoveItem() extends Cmd

}

class Dashboard(
   var name: String,
   var description: String,
   var style: String,
   var items: Map[String, Item] = Map(),
   var ownerName: String,
   var ownerEmail: String,
   var readonlyHash: String,
   var writeHash: String
)