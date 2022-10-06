package com.example.HolidayManager.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {
    @Bean
    public AmazonSimpleEmailService emailService(){

        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withCredentials(credentialsProvider())
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    public AWSCredentialsProvider credentialsProvider(){
        return new AWSStaticCredentialsProvider(credentials());
    }

    public AWSCredentials credentials(){

        return new BasicAWSCredentials("AKIA46VVTN5UL6DA7NOL","2XPdu8SaffqyhKYlISpuV0otsPo+NrYNAM4n4vuV");
    }
}

