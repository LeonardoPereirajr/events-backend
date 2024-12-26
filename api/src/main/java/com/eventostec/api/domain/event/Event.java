package com.eventostec.api.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "event")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Event {
    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private String description;
    private String imgUrl;
    private String eventUrl;
    private Boolean remote;
    private Date date;

      public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setEventUrl(String eventUrl) {
        this.eventUrl = eventUrl;
    }

    public void setRemote(Boolean remote) {
        this.remote = remote;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public UUID getId() {
        return id;
    }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImgUrl() { return imgUrl; }
    public String getEventUrl() { return eventUrl; }
    public Boolean getRemote() { return remote; }
    public Date getDate() { return date; }
}
