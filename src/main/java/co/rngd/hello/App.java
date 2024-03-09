package co.rngd.hello;

import java.util.*;

public class App {
    public static void main(String[] args) {
        var src = """
            message = (hello), (world)!
            hello = Hello | Hi | Greetings
            world = World | Everybody | Java""";

        var parsed = Parser.parse(src);

        var message = parsed.get("message");

        var r = new Random();
        for (int i = 0; i < 10; i++)
            System.out.println(message.generate(r));
    }
}
