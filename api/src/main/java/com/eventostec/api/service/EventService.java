package com.eventostec.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.address.Address;
import com.eventostec.api.domain.coupon.Coupon;
import com.eventostec.api.domain.event.*;
import com.eventostec.api.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private AddressService addressService;

    public Event createEvent(EventRequestDTO data) {
        logger.info("Iniciando criação do evento...");
        logger.info("Dados recebidos: título={}, descrição={}, data={}, cidade={}, estado={}, remoto={}, URL={}, imagem={}",
                data.title(), data.description(), data.date(), data.city(), data.state(), data.remote(), data.eventUrl(),
                (data.image() != null ? data.image().getOriginalFilename() : "Nenhuma"));

        String imgUrl = "default-image-url";

        if (data.image() != null) {
            try {
                logger.info("Arquivo de imagem recebido: {}", data.image().getOriginalFilename());
                imgUrl = this.uploadImg(data.image());
                logger.info("Imagem carregada com sucesso. URL da imagem: {}", imgUrl);
            } catch (Exception e) {
                logger.error("Erro ao carregar a imagem: {}", e.getMessage(), e);
            }
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

        try {
            newEvent = repository.save(newEvent);
            logger.info("Evento criado e salvo no banco de dados com sucesso. ID: {}", newEvent.getId());
        } catch (Exception e) {
            logger.error("Erro ao salvar o evento no banco de dados: {}", e.getMessage(), e);
            throw e;
        }

        if (Boolean.FALSE.equals(data.remote())) {
            logger.info("O evento não é remoto. Iniciando criação do endereço...");
            try {
                this.addressService.createAddress(data, newEvent);
                logger.info("Endereço criado com sucesso para o evento ID: {}", newEvent.getId());
            } catch (Exception e) {
                logger.error("Erro ao criar o endereço para o evento ID: {}: {}", newEvent.getId(), e.getMessage(), e);
            }
        } else {
            logger.info("O evento é remoto. Nenhum endereço será criado.");
        }

        return newEvent;
    }
    @Autowired
    private CouponService couponService;

    public EventDetailsDTO getEventDetails(UUID eventId) {
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        List<Coupon> coupons = couponService.consultCoupons(eventId, new Date());

        List<EventDetailsDTO.CouponDTO> couponDTOs = coupons.stream()
                .map(coupon -> new EventDetailsDTO.CouponDTO(
                        coupon.getCode(),
                        coupon.getDiscount(),
                        coupon.getValid()))
                .collect(Collectors.toList());

        return new EventDetailsDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getAddress()!= null? event.getAddress().getCity() : "",
                event.getAddress()!= null? event.getAddress().getUf(): "",
                event.getImgUrl(),
                event.getEventUrl(),
                couponDTOs);
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

    public List<EventResponseDTO> getFilteredEvents(int page, int size, String city, String uf, Date startDate, Date endDate){
        city = (city != null) ? city : "";
        uf = (uf != null) ? uf : "";
        startDate = (startDate != null) ? startDate : new Date(0);
        endDate = (endDate != null) ? endDate : new Date();

        Pageable pageable = PageRequest.of(page, size);

        Page<EventAddressProjection> eventsPage = this.repository.findFilteredEvents(city, uf, startDate, endDate, pageable);
        return eventsPage.map(event -> new EventResponseDTO(
                        event.getId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDate(),
                        event.getCity() != null ? event.getCity() : "",
                        event.getUf() != null ? event.getUf() : "",
                        event.getRemote(),
                        event.getEventUrl(),
                        event.getImgUrl())
                )
                .stream().toList();
    }



    public List<EventResponseDTO> getUpcomingEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EventAddressProjection> eventsPage = this.repository.findUpcomingEvents(new Date(), pageable);
        return eventsPage.map(event -> new EventResponseDTO(
                        event.getId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDate(),
                        event.getCity() != null ? event.getCity() : "",
                        event.getUf() != null ? event.getUf() : "",
                        event.getRemote(),
                        event.getEventUrl(),
                        event.getImgUrl())
                )
                .stream().toList();
    }
}
