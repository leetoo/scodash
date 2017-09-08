package pojo

class Item(var name: String, var score: Int) {

  def this(name: String ) {
    this(name, 0)
  }

  def increment(): Unit = this.score = this.score + 1

  def decrement(): Unit = if (this.score > 0) this.score = this.score - 1


}