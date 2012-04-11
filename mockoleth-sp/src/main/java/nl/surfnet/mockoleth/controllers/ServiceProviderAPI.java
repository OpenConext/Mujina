package nl.surfnet.mockoleth.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import nl.surfnet.mockoleth.model.SpConfiguration;

@Controller
public class ServiceProviderAPI {

    private final static Logger log = LoggerFactory
            .getLogger(ServiceProviderAPI.class);

    final SpConfiguration configuration;

    @Autowired
    public ServiceProviderAPI(final SpConfiguration configuration) {
        this.configuration = configuration;
    }
}
