package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.phoneworld.model.Phone;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface INFBoardgameMongoDB extends MongoRepository<Phone, String> {

    List<Phone> findByReleaseYear(int releaseYear);
    List<Phone> findByBrandOrderByReleaseYearDesc(String brand);

    @Query(value = "{'name': {$regex : ?0, $options: 'i'}}")
    List<Phone> findByNameRegexOrderByReleaseYearDesc(String name, Sort sort);

    @Query(value = "{'ram': {$regex : /.*?0.*GB/, $options: 'i'}}")
    List<Phone> findByRamRegexOrderByReleaseYearDesc(String ram, Sort sort);

    @Query(value = "{'storage': {$regex : /^?0GB.*/, $options: 'i'}}")
    List<Phone> findByStorageRegexOrderByReleaseYearDesc(String storage, Sort sort);

    @Query(value = "{'chipset': {$regex : ?0, $options: 'i'}}")
    List<Phone> findByChipsetRegexOrderByReleaseYearDesc(String chipset, Sort sort);

    @Query(value = "{'batterySize': {$regex : /^?0 mAh.*/, $options: 'i'}}")
    List<Phone> findByBatterySizeRegexOrderByReleaseYearDesc(String batterySize, Sort sort);

    @Query(value = "{'cameraPixels': {$regex : /^?0 MP.*/, $options: 'i'}}")
    List<Phone> findByCameraPixelsRegexOrderByReleaseYearDesc(String cameraPixels, Sort sort);

    Optional<Phone> findByName(String name);

}
