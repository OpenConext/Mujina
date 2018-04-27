package mujina.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter
@ToString
@AllArgsConstructor
public class User implements Serializable {

  private String name;
  private String password;
  private List<String> authorities;
}
