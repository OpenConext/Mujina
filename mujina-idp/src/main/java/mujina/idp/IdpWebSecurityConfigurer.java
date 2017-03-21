package mujina.idp;

import mujina.api.IdpConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableWebSecurity
public class IdpWebSecurityConfigurer extends WebMvcConfigurerAdapter {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/idp/login").setViewName("login");
  }

  @Configuration
  @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
  protected static class ApplicationSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
    private IdpConfiguration idpConfiguration;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
        .antMatcher("/idp/**")
        .authorizeRequests()
        .antMatchers("/idp/SingleSignOnService", "/idp/metadata", "idp/api/**", "/resources/**").permitAll()
        .antMatchers("/idp/admin/**").hasRole("ADMIN")
        .anyRequest().hasRole("USER")
        .and()
        .formLogin()
        .loginPage("/idp/login")
        .permitAll()
        .failureUrl("/idp/error")
        .permitAll()
        .and()
        .logout()
        .logoutSuccessUrl("/idp/index");
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth.authenticationProvider(new IdpAuthenticationProvider(idpConfiguration));
      //auth.inMemoryAuthentication().withUser("user").password("user").roles("USER");
    }

  }

}
