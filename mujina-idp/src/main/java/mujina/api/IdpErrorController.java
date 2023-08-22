package mujina.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/error")
public class IdpErrorController extends ErrorController {

    @Autowired
    public IdpErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }
}
