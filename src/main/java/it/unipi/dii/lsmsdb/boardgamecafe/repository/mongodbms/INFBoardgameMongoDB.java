/*
package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

//import it.unipi.dii.lsmsdb.phoneworld.model.Phone;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.Boardgame;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface INFBoardgameMongoDB extends MongoRepository<Boardgame, String> {

    List<Boardgame> findByReleaseYear(int releaseYear);
    List<Boardgame> findByBrandOrderByReleaseYearDesc(String brand);

    @Query(value = "{'name': {$regex : ?0, $options: 'i'}}")
    List<Boardgame> findByNameRegexOrderByReleaseYearDesc(String name, Sort sort);

    @Query(value = "{'ram': {$regex : /.*?0.*GB/, $options: 'i'}}")
    List<Boardgame> findByRamRegexOrderByReleaseYearDesc(String ram, Sort sort);
*/

//@Query(value = "{'storage': {$regex : /^?0GB.*/, $options: 'i'}}")
//List<Boardgame> findByStorageRegexOrderByReleaseYearDesc(String storage, Sort sort);

//@Query(value = "{'chipset': {$regex : ?0, $options: 'i'}}")
//List<Boardgame> findByChipsetRegexOrderByReleaseYearDesc(String chipset, Sort sort);

//@Query(value = "{'batterySize': {$regex : /^?0 mAh.*/, $options: 'i'}}")
//List<Boardgame> findByBatterySizeRegexOrderByReleaseYearDesc(String batterySize, Sort sort);

//@Query(value = "{'cameraPixels': {$regex : /^?0 MP.*/, $options: 'i'}}")
//List<Boardgame> findByCameraPixelsRegexOrderByReleaseYearDesc(String cameraPixels, Sort sort);

//Optional<Boardgame> findByName(String name);
//}
