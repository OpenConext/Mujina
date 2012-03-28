package nl.surfnet.mockoleth.controllers;

import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.surfnet.mockoleth.model.Attribute;
import nl.surfnet.mockoleth.model.Configuration;

@Controller
public class RestApiController {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(RestApiController.class);

    @Autowired
    Configuration configuration;

    @RequestMapping("/test")
    public String test() {
        return "test-view";
    }

    @RequestMapping(value = {"/set-attribute"}, method = RequestMethod.POST)
    @ResponseBody
    public void setAttribute(@RequestBody Attribute attribute, HttpServletResponse response) {
        LOGGER.info("Request to set attribute {} to {}", attribute.getValue(), attribute.getName());
        configuration.getAttributes().put(attribute.getName(), attribute.getValue());
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @RequestMapping(value = {"/reset"}, method = RequestMethod.POST)
    @ResponseBody
    public void reset(HttpServletResponse response) {
        LOGGER.info("Resetting to default configuration");
        configuration.reset();
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
