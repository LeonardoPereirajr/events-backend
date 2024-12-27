package com.eventostec.api.domain.coupon;

import com.eventostec.api.domain.event.Event;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "coupon")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue
    private UUID id;

    private String code;
    private Integer discount;
    private Date valid;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    public String getCode() {
        return code;
    }

    public Integer getDiscount() {
        return discount;
    }

    public Date getValid() {
        return valid;
    }

    public Event getEvent() {
        return event;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public void setValid(Date valid) {
        this.valid = valid;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}