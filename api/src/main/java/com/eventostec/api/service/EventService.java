package com.eventostec.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private EventRepository repository;

    public Event createEvent(EventRequestDTO data) {
        logger.info("Iniciando criação do evento...");
        logger.info("Dados recebidos: título={}, descrição={}, data={}, cidade={}, estado={}, remoto={}, URL={}, imagem={}",
                data.title(), data.description(), data.date(), data.city(), data.state(), data.remote(), data.eventUrl(), data.image());

        String imgUrl = "default-image-url";

        if (data.image() != null) {
            logger.info("Arquivo de imagem recebido: {}", data.image().getOriginalFilename());
            imgUrl = this.uploadImg(data.image());
        } else {
            logger.warn("Nenhuma imagem foi enviada. Usando URL padrão.");
        }

        Event newEvent = new Event();
        newEvent.setTitle(data.title());
        newEvent.setDescription(data.description());
        newEvent.setEventUrl(data.eventUrl());
        newEvent.setDate(new Date(data.date()));
        newEvent.setImgUrl(imgUrl);
        newEvent.setRemote(data.remote());

        newEvent = repository.save(newEvent);
        logger.info("Evento criado e salvo no banco de dados com sucesso. ID: {}", newEvent.getId());
        return newEvent;

    }

    private String uploadImg(MultipartFile multipartFile) {
        String filename = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();
        logger.info("Iniciando upload da imagem com nome gerado: {}", filename);

        try {
            File file = this.convertMultiPartToFile(multipartFile);
            logger.info("Convertendo MultipartFile para File. Tamanho do arquivo: {} bytes", file.length());

            s3Client.putObject(bucketName, filename, file);
            logger.info("Upload para o bucket {} realizado com sucesso. Nome do arquivo: {}", bucketName, filename);

            file.delete();
            logger.info("Arquivo local deletado após upload: {}", file.getName());

            return s3Client.getUrl(bucketName, filename).toString();
        } catch (Exception e) {
            logger.error("Erro ao subir arquivo para o bucket: {}", e.getMessage(), e);
            return "default-image-url";
        }
    }

    private File convertMultiPartToFile(MultipartFile multipartFile) throws IOException {

        File convFile = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(multipartFile.getBytes());
        fos.close();
        return convFile;
    }
}
