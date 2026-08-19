package mujina.sp;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import mujina.saml.SAMLAttribute;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.util.List;

@Getter
@EqualsAndHashCode(of = "nameID")
public class SamlPrincipal implements AuthenticatedPrincipal {

    private final String nameID;
    private final String nameIDType;
    private final List<SAMLAttribute> attributes;

    public SamlPrincipal(String nameID, String nameIDType, List<SAMLAttribute> attributes) {
        this.nameID = nameID;
        this.nameIDType = nameIDType;
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return nameID;
    }
}
