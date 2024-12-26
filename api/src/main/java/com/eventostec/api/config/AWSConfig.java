package com.eventostec.api.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class AWSConfig {

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

    private static final Logger logger = LoggerFactory.getLogger(AWSConfig.class);

    @Bean
    public AmazonS3 createS3instance() {
        logger.info("Inicializando o cliente AmazonS3...");
        logger.info("Região configurada: {}", awsRegion);
        logger.info("Access Key ID: {}", accessKeyId.substring(0, 4) + "****");

        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder
                    .standard()
                    .withRegion(awsRegion)
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(accessKeyId, secretAccessKey)
                    ))
                    .build();
            logger.info("Cliente AmazonS3 inicializado com sucesso.");
            return s3Client;
        } catch (Exception e) {
            logger.error("Erro ao inicializar o cliente AmazonS3: {}", e.getMessage(), e);
            throw e;
        }
    }
}
