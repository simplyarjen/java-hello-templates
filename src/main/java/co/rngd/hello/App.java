package co.rngd.hello;

import java.util.*;

public class App {
    public static void main(String[] args) {
        var parser = new Parser();

        var src = """
            message = (hello), (world)!
            hello = Hello | Hi | Greetings
            world = World | Everybody | Java""";

        parser.parse(src);

        var message = parser.getTemplate("message");

        var r = new Random();
        for (int i = 0; i < 10; i++)
            System.out.println(message.generate(r));
    }
}
