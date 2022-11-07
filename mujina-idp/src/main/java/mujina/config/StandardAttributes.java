package mujina.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConfigurationProperties(prefix = "idp")
public class StandardAttributes {

    private Map<String, String> attributes;

    private final Pattern escapedValuePattern = Pattern.compile("\\[(.*)]");

    public void setAttributes(Map<String, String> attributes) {
        Map<String, String> processedAttributes = new HashMap<>();

        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            Matcher matcher = escapedValuePattern.matcher(attribute.getKey());
            processedAttributes.put(matcher.matches() ? matcher.group(1) : attribute.getKey(), attribute.getValue());
        }

        this.attributes = processedAttributes;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
