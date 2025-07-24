package com.example.demo;  
  
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.example.demo.security.TotpAuthenticator;  
  
@SpringBootApplication  
public class DemoApplication {  
  
    public static void main(String[] args) {  
        ApplicationContext context = SpringApplication.run(DemoApplication.class, args);  
        String[] beanNames = context.getBeanNamesForType(TotpAuthenticator.class);  
        for (String beanName : beanNames) {  
            System.out.println("Found bean: " + beanName);  
        }
    }
}