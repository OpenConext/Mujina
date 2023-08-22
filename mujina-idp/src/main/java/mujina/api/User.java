package mujina.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private String name;
    private String password;
    private List<String> authorities;
}
