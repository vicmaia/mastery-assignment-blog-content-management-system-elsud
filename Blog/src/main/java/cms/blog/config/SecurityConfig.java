package cms.blog.config;

import cms.blog.dto.User;
import cms.blog.service.ServiceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;



@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    ServiceLayer serviceLayer;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        User admin = serviceLayer.getUserByName("admin");
        User manager = serviceLayer.getUserByName("manager");
        if (admin==null || manager==null) {
            return;
        }
        auth
                .inMemoryAuthentication()
                .withUser(manager.getEmail()).password("{noop}" + manager.getPassword()).roles("MANAGER").and()
                .withUser(admin.getEmail()).password("{noop}" + admin.getPassword()).roles("ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/", "/home", "/blog/**").permitAll()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/process-login")
                //.defaultSuccessUrl("/")
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


