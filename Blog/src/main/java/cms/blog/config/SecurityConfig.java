package cms.blog.config;

import cms.blog.dto.User;
import cms.blog.service.ServiceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


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
        //System.out.println(passwordEncoder().encode("1111")); //$2a$10$8gFS6nYtAsDYSnd8y1eMuu/Ey720LdIIPeQ0YCdCQ0hq6JkLiSEUy
        //System.out.println(passwordEncoder().encode("2222")); //$2a$10$qldjV4W4oMxmleDWd8k/vuKyTV/1oQTOQ.46Mv4wvMZ6l.i/rfpqS
        auth
                .inMemoryAuthentication()
                //.withUser(manager.getEmail()).password(passwordEncoder().encode(manager.getPassword())).roles("MANAGER").and()
                .withUser(manager.getEmail()).password("{noop}" + manager.getPassword()).roles("MANAGER").and()
                //.withUser(admin.getEmail()).password(passwordEncoder().encode(admin.getPassword())).roles("ADMIN")
                .withUser(admin.getEmail()).password("{noop}" + admin.getPassword()).roles("ADMIN");
    }

    //@Bean
    //public PasswordEncoder passwordEncoder() {
    //    return new BCryptPasswordEncoder();
    //}

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


