package bul.nik.ldtesttask.dadata.repository;

import bul.nik.ldtesttask.dadata.model.PhoneNumberData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DadataRepository extends JpaRepository<PhoneNumberData, UUID> {

}
