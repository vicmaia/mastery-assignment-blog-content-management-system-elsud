package cms.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser("user").password("{noop}1111").roles("MANAGER").and()
                .withUser("admin")
                .password("{noop}2222")
                .roles("ADMIN");
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/", "/home").permitAll()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/process-login")
                .defaultSuccessUrl("/home")
                .permitAll()
                .and()
                .logout()
                .permitAll()
                .and()
                .csrf()
                .disable();
        http.authorizeRequests().antMatchers("/admin/**").hasRole("ADMIN").and().formLogin();
        http.authorizeRequests().antMatchers("/manager/**").hasRole("MANAGER").and().formLogin();
    }

}


