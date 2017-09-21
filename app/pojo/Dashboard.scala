package pojo

import java.util.UUID

import scala.collection.mutable

object Dashboard {

  trait Cmd

  case class Watch() extends Cmd
  case class Unwatch() extends Cmd
  case class Data(items: mutable.Map[String, Item]) extends Cmd
  case class IncrementItem(name: String) extends Cmd
  case class DecrementItem(name: String) extends Cmd
  case class AddItem(name: String) extends Cmd
  case class RemoveItem(name: String) extends Cmd
  case class GetWriteHash() extends Cmd
  case class GetReadonlyHash() extends Cmd
  case class GetName() extends Cmd
  case class GetDashboard() extends Cmd

  def apply(writeHash: String) : Dashboard = {
    new Dashboard(writeHash = writeHash);
  }

}

@SerialVersionUID(100L)
class Dashboard (
   var name: String,
   var description: String,
   var style: String,
   var items: mutable.Map[String, Item] = mutable.Map(),
   var ownerName: String,
   var ownerEmail: String,
   var readonlyHash: String,
   var writeHash: String
)  extends Serializable