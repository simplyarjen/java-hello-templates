package co.rngd.hello;

import static co.rngd.hello.Template.*;

import java.util.Random;

public class App {
    public static void main(String[] args) {

        var hello = choose(
            literal("Hello"), 
            literal("Hi"), 
            literal("Greetings"));
        var world = choose(
            literal("world"),
            literal("Java"),
            literal("everybody"));
        var message = join(hello, literal(", "), world);

        var r = new Random();
        for (int i = 0; i < 10; i++)
            System.out.println(message.generate(r));
    }
}

interface Template {
    String generate(Random random);
    
    static Template choose(Template... options) {
        return r -> options[r.nextInt(options.length)].generate(r);
    }

    static Template join(Template... fragments) {
        return r -> { 
            var result = new StringBuilder();
            for (Template fragment : fragments) {
                result.append(fragment.generate(r));
            }
            return result.toString();
        };
    }

    static Template literal(String value) {
        return r -> value;
    }
}