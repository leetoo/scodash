package controllers

import java.io.IOException

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import akka.persistence.PersistentActor
import com.sendgrid._
import com.typesafe.config.Config


trait EmailSupport{ me:PersistentActor =>

  val emailSettings = EmailSettings(context.system)

  def sendEmailNewDashboard(toAddress: String, readHash: String, writeHash: String, dashName: String, ownerName: String): Unit = {
    val from = new Email("info@scodash.com");
    val subject = s"$dashName dashboard";
    val to = new Email(toAddress);
    val readUrl = s"${emailSettings.baseUrl}$readHash"
    val writeUrl = s"${emailSettings.baseUrl}$writeHash"
    val content = new Content("text/plain", s"Hello ${ownerName}, \n\rYou can share your dashboard $dashName with your friends via these links:\n\rRead-only " +
      s"mode: $readUrl\n\rWrite " +
      s"mode: $writeUrl\n\r\n\rEnjoy scoring!");
    val mail = new Mail(from, subject, to, content);

    val sendGrid = new SendGrid(emailSettings.apiKey)
    val request = new Request()
    try {
      request.setMethod(Method.POST)
      request.setEndpoint("mail/send")
      request.setBody(mail.build)
      val response = sendGrid.api(request)
      System.out.println(response.getStatusCode)
      System.out.println(response.getBody)
      System.out.println(response.getHeaders)
    } catch {
      case ex: IOException =>
        throw ex
    }

  }

}

class EmailSettingsImpl(conf:Config) extends Extension{
  val emailConfig = conf.getConfig("email")
  val apiKey = System.getenv("SENDGRID_API_KEY")
  val baseUrl = emailConfig.getString("base.url")
}
object EmailSettings extends ExtensionId[EmailSettingsImpl] with ExtensionIdProvider {
  override def lookup = EmailSettings
  override def createExtension(system: ExtendedActorSystem) =
    new EmailSettingsImpl(system.settings.config)
}