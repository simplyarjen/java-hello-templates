package co.rngd.hello;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public interface Template {
     String generate(Random random);
    
    static Template choose(Template... options) {
        return r -> options[r.nextInt(options.length)].generate(r);
    }

    static Template join(Template... fragments) {
        return join(Arrays.asList(fragments));
    }

    static Template join(Collection<Template> fragments) {
    
        return r -> { 
            var result = new StringBuilder();
            fragments.forEach(fragment -> 
                result.append(fragment.generate(r)));
            return result.toString();
        };
    }

    static Template literal(String value) {
        return r -> value;
    }

    static Map<String, Template> parse(String source) {
        class Thunk implements Template {
            private Template resolved;
            public String generate(Random r) { return resolved.generate(r); }
            private void resolve(Template t) { this.resolved = t; }
            static boolean isUnresolved(Template t) {
                return (t instanceof Thunk && ((Thunk) t).resolved == null);
            }
        }
        
        var result = new HashMap<String, Template>();

        source.lines().forEach(line -> {
            var parts = line.split(" *= *", 2);
            if (parts.length < 2) {
                throw new IllegalArgumentException("Malformed template: " + line);
            }
            var name = parts[0].trim();
            var choices = parts[1].split(" *\\| *");
            var choiceTemplates = new Template[choices.length];

            for (int i = 0; i < choices.length; i++) {
                var literal = true;
                var fragment = new StringBuilder();
                var fragmentTemplates = new ArrayList<Template>();
                for (int j = 0; j < choices[i].length(); j++) {
                    var c = choices[i].charAt(j);
                    if (c == '(') {
                        if (!literal) throw new IllegalArgumentException("Invalid template: " + choices[i]);
                        if (!fragment.isEmpty()) {
                            fragmentTemplates.add(literal(fragment.toString()));
                        }
                        fragment.setLength(0);
                        literal = false;
                    }
                    else if (c == ')') {
                        if (literal) throw new IllegalArgumentException("Invalid template: " + choices[i]);
                        var reference = fragment.toString();
                        result.putIfAbsent(reference, new Thunk());
                        fragmentTemplates.add(result.get(reference));
                        fragment.setLength(0);
                        literal = true;
                    }
                    else {
                        fragment.append(c);
                    }
                }
                if (!literal) throw new IllegalArgumentException("Invalid template: " + choices[i]);
                if (!fragment.isEmpty()) {
                    fragmentTemplates.add(literal(fragment.toString()));
                }
                choiceTemplates[i] = (fragmentTemplates.size() == 1) ? fragmentTemplates.get(0) : join(fragmentTemplates);
            }

            var template = choices.length == 1 ? choiceTemplates[0] : choose(choiceTemplates);

            if (!result.containsKey(name)) {
                result.put(name, template);
            }
            else if (Thunk.isUnresolved(result.get(name))) {
                ((Thunk) result.get(name)).resolve(template);
            }
            else {
                throw new IllegalArgumentException("Duplicate template definition for: " + name);
            }
        });

        var unresolved = result.entrySet().stream()
            .filter(e -> Thunk.isUnresolved(e.getValue()))
            .map(Map.Entry::getKey)
            .toList();
        if (!unresolved.isEmpty()) {
            throw new IllegalArgumentException("Unresolved templates: " + unresolved);
        }
        return result;
    }
}
