package nl.surfnet.mockoleth.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.surfnet.mockoleth.model.CommonConfiguration;
import nl.surfnet.mockoleth.model.Credential;
import nl.surfnet.mockoleth.model.EntityID;

@Controller
public class CommonAPI {

    private final static Logger log = LoggerFactory.getLogger(CommonAPI.class);

    private CommonConfiguration configuration;

    @Autowired
    public CommonAPI(final CommonConfiguration configuration) {
        this.configuration = configuration;
    }

    @RequestMapping(value = {"/reset"}, method = RequestMethod.POST)
    @ResponseBody
    public void reset() {
        log.info("Resetting to default configuration");
        configuration.reset();
    }

    @RequestMapping(value = {"/set-entityid"}, method = RequestMethod.POST)
    @ResponseBody
    public void setEntityID(@RequestBody EntityID entityID) {
        log.info("Request to set entityID {}", entityID.getValue());
        configuration.setEntityID(entityID.getValue());
    }

    @RequestMapping(value = {"/set-signing-credential"}, method = RequestMethod.POST)
    @ResponseBody
    public void setSigningCredential(@RequestBody Credential credential) {
        log.info("Request to set signing credential");
        configuration.injectCredential(credential.getCertificate(), credential.getKey());
    }


}
