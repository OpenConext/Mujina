package mujina.api.idp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@ToString
public class User implements Serializable {

  private String name;
  private String password;
  private List<String> authorities;
}
