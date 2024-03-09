package co.rngd.hello;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Parser {
    private static final class TemplateReference implements Template {
        private Template resolved;
        public StringBuilder generate(StringBuilder builder, Random random) { return resolved.generate(builder, random); }
        private void resolve(Template t) { this.resolved = t; }
        static boolean isUnresolved(Template t) {
            return (t instanceof TemplateReference && ((TemplateReference) t).resolved == null);
        }
    }
    
    private final Map<String, Template> templates = new HashMap<>();

    public void parse(String source) {
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
                            fragmentTemplates.add(Template.literal(fragment.toString()));
                        }
                        fragment.setLength(0);
                        literal = false;
                    }
                    else if (c == ')') {
                        if (literal) throw new IllegalArgumentException("Invalid template: " + choices[i]);
                        var reference = fragment.toString();
                        templates.putIfAbsent(reference, new TemplateReference());
                        fragmentTemplates.add(templates.get(reference));
                        fragment.setLength(0);
                        literal = true;
                    }
                    else {
                        fragment.append(c);
                    }
                }
                if (!literal) throw new IllegalArgumentException("Invalid template: " + choices[i]);
                if (!fragment.isEmpty()) {
                    fragmentTemplates.add(Template.literal(fragment.toString()));
                }
                choiceTemplates[i] = (fragmentTemplates.size() == 1) ? fragmentTemplates.get(0) : Template.join(fragmentTemplates);
            }

            var template = choices.length == 1 ? choiceTemplates[0] : Template.choose(choiceTemplates);

            if (!templates.containsKey(name)) {
                templates.put(name, template);
            }
            else if (TemplateReference.isUnresolved(templates.get(name))) {
                ((TemplateReference) templates.get(name)).resolve(template);
            }
            else {
                throw new IllegalArgumentException("Duplicate template definition for: " + name);
            }
        });

        var unresolved = templates.entrySet().stream()
            .filter(e -> TemplateReference.isUnresolved(e.getValue()))
            .map(Map.Entry::getKey)
            .toList();
        if (!unresolved.isEmpty()) {
            throw new IllegalArgumentException("Unresolved templates: " + unresolved);
        }
    }

    public Template getTemplate(String name) {
        return templates.get(name);
    }
}
