package nl.surfnet.mockoleth.controllers;

import java.io.UnsupportedEncodingException;

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
    public String test() throws UnsupportedEncodingException {
        return "test-view";
    }

    @RequestMapping(value = {"/set-attribute"}, method = RequestMethod.POST)
    @ResponseBody
    public String setAttribute(@RequestBody Attribute attribute) throws UnsupportedEncodingException {
        LOGGER.info("Request to set attribute {} to {}", attribute.getValue(), attribute.getName());
        configuration.getAttributes().put(attribute.getValue(), attribute.getName());
        return "success";
    }

    @RequestMapping(value = {"/reset"}, method = RequestMethod.POST)
    @ResponseBody
    public String reset() throws UnsupportedEncodingException {
        LOGGER.info("Resetting to default configuration");
        return "success";
    }
}
