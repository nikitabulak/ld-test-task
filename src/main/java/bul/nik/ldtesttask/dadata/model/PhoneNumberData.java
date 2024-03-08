package bul.nik.ldtesttask.dadata.model;

import bul.nik.ldtesttask.user.model.User;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import javax.persistence.*;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "phone_number_data")
public class PhoneNumberData {
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;
    @OneToOne(mappedBy = "phoneNumberData")
    private User owner;
    @Column(name = "country_code")
    private String countryCode;
    @Column(name = "city_code")
    private String cityCode;
    @Column(name = "phone")
    private String phone;

}
