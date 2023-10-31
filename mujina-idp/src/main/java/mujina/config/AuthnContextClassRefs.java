package mujina.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "acr")
@Getter
@Setter
public class AuthnContextClassRefs {

    private List<String> values;
}
