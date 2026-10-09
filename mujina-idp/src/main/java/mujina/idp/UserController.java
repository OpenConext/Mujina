package mujina.idp;

import mujina.config.AuthnContextClassRefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;

@Controller
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final List<Map<String, String>> samlAttributes;
    private final AuthnContextClassRefs authnContextClassRefs;
    private final String filePersonasJson;

    @Autowired
    @SuppressWarnings("unchecked")
    public UserController(JsonMapper jsonMapper,
                          AuthnContextClassRefs authnContextClassRefs,
                          @Value("${idp.saml_attributes_config_file}") String samlAttributesConfigFile) throws IOException {

        DefaultResourceLoader loader = new DefaultResourceLoader();
        this.samlAttributes = jsonMapper.readValue(
                loader.getResource(samlAttributesConfigFile).getInputStream(), new TypeReference<>() {
                });
        this.samlAttributes.sort(comparing(m -> m.get("id")));
        this.authnContextClassRefs = authnContextClassRefs;
        Resource[] resources = new Resource[DEFAULT_PERSONAS_LOCATIONS.length];
        for (int i = 0; i < DEFAULT_PERSONAS_LOCATIONS.length; i++) {
            resources[i] = loader.getResource(DEFAULT_PERSONAS_LOCATIONS[i]);
        }
        this.filePersonasJson = loadFilePersonas(jsonMapper, resources);
    }

    private static final String[] DEFAULT_PERSONAS_LOCATIONS = {"file:./personas.json", "classpath:personas.json"};

    static String loadFilePersonas(JsonMapper jsonMapper, Resource[] resources) {
        // A personas.json bind-mounted into the container's working directory (the same
        // way application.yml / logback.xml are overridden) takes precedence over the one
        // bundled inside the JAR. If neither is present there are no bundled personas.
        for (Resource resource : resources) {
            if (!resource.exists()) {
                continue;
            }
            try (InputStream in = resource.getInputStream()) {
                return jsonMapper.writeValueAsString(jsonMapper.readValue(in, new TypeReference<>() {
                }));
            } catch (Exception e) {
                LOG.warn("Optional personas.json could not be read: {}", e.getMessage());
            }
        }
        return null;
    }

    @GetMapping("/")
    public String index(Authentication authentication) {
        return authentication == null ? "index" : "redirect:/user.html";
    }

    @GetMapping("/user.html")
    public String user(Authentication authentication, ModelMap modelMap) {
        modelMap.addAttribute("user", authentication);
        return "user";
    }

    @GetMapping("/login")
    public String login(ModelMap modelMap) {
        modelMap.addAttribute("samlAttributes", samlAttributes);
        modelMap.addAttribute("authnContextClassRefs", authnContextClassRefs.getValues());
        modelMap.addAttribute("filePersonasJson", filePersonasJson);
        return "login";
    }
}
