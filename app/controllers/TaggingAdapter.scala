package controllers

import akka.persistence.journal.{Tagged, WriteEventAdapter}

class TaggingAdapter extends WriteEventAdapter {
  override def manifest(event: Any) = event.getClass.getName

  override def toJournal(event: Any) = event match {
    case ev:EntityEvent =>
      //Add tags for the entity type and the event class name
      val eventType = ev.getClass.getName().toLowerCase().split("\\$").last
      Tagged(event, Set(ev.entityType, eventType))
    case _ => throw new RuntimeException(s"Tagging adapter can't write adapt type: $event")
  }
}
