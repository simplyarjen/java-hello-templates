package co.rngd.hello;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public interface Template {
    StringBuilder generate(StringBuilder builder, Random random);
    
    default String generate(Random random) {
        return generate(new StringBuilder(), random).toString(); 
    }
    
    static Template choose(Template... options) {
        return (builder, random) -> options[random.nextInt(options.length)].generate(builder, random);
    }

    static Template join(Template... fragments) {
        return join(Arrays.asList(fragments));
    }

    static Template join(Collection<Template> fragments) {
        return (builder, random) -> { 
            fragments.forEach(fragment -> fragment.generate(builder, random));
            return builder;
        };
    }

    static Template literal(String value) {
        return (builder, random) -> builder.append(value);
    }
}
