package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.*;


public class Application extends Controller {

//    public static Result index() {
//        return ok(index.render("Your new application is ready."));
//    }

    public static WebSocket<String> socket() {
        return WebSocket.whenReady((in, out) -> {
            in.onMessage(System.out::println);
            in.onClose(() -> System.out.println("Disconnected"));
            out.write("Hello!");
        });
    }

}
