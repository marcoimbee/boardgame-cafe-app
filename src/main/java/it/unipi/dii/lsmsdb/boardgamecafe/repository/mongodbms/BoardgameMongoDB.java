/*
package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.phoneworld.model.Phone;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
public class BoardgameMongoDB {

    public PhoneMongo() {
    }

    private final static Logger logger = LoggerFactory.getLogger(PhoneMongo.class);

    @Autowired
    private IPhoneMongo phoneMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public IPhoneMongo getPhoneMongo() {
        return phoneMongo;
    }

    public boolean addPhone(Phone phone) {
        boolean result = true;
        try {
            phoneMongo.save(phone);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public List<Phone> findPhones(String name, String parameter) {
        List<Phone> phones = new ArrayList<>();
        try {
            if (name.isEmpty()) {
                phones.addAll(findRecentPhones());
            } else if (parameter.equals("Name")){
                phones.addAll(phoneMongo.findByNameRegexOrderByReleaseYearDesc(name,
                        Sort.by(Sort.Direction.DESC, "releaseYear")));
            } else if (parameter.equals("Ram (GB)")){
                phones.addAll(phoneMongo.findByRamRegexOrderByReleaseYearDesc(name,
                        Sort.by(Sort.Direction.DESC, "releaseYear")));
            } else if (parameter.equals("Storage (GB)")){
                phones.addAll(phoneMongo.findByStorageRegexOrderByReleaseYearDesc(name,
                        Sort.by(Sort.Direction.DESC, "releaseYear")));
            } else if (parameter.equals("Chipset")){
                phones.addAll(phoneMongo.findByChipsetRegexOrderByReleaseYearDesc(name,
                        Sort.by(Sort.Direction.DESC, "releaseYear")));
            } else if (parameter.equals("Battery Size (mAh)")){
                phones.addAll(phoneMongo.findByBatterySizeRegexOrderByReleaseYearDesc(name,
                        Sort.by(Sort.Direction.DESC, "releaseYear")));
            } else if (parameter.equals("Camera Pixels (MP)")){
                phones.addAll(phoneMongo.findByCameraPixelsRegexOrderByReleaseYearDesc(name,
                        Sort.by(Sort.Direction.DESC, "releaseYear")));
            } else if (parameter.equals("Release Year")){
                phones.addAll(phoneMongo.findByReleaseYear(Integer.parseInt(name)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phones;
    }

    public Optional<Phone> findPhoneById(String id) {
        Optional<Phone> phone = Optional.empty();
        try {
            phone = phoneMongo.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phone;
    }

    public Optional<Phone> findPhoneByName(String name) {
        Optional<Phone> phone = Optional.empty();
        try {
            phone = phoneMongo.findByName(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phone;
    }

    public boolean updatePhone(String id, Phone newPhone) {
        boolean result = true;
        try {
            Optional<Phone> phone = phoneMongo.findById(id);
            if (phone.isPresent()) {
                phone.get().setBrand(newPhone.getBrand());
                phone.get().setName(newPhone.getName());
                phone.get().setPicture(newPhone.getPicture());
                phone.get().setReleaseYear(newPhone.getReleaseYear());
                phone.get().setBody(newPhone.getBody());
                phone.get().setOs(newPhone.getOs());
                phone.get().setStorage(newPhone.getStorage());
                phone.get().setDisplaySize(newPhone.getDisplaySize());
                phone.get().setDisplayResolution(newPhone.getDisplayResolution());
                phone.get().setCameraPixels(newPhone.getCameraPixels());
                phone.get().setVideoPixels(newPhone.getVideoPixels());
                phone.get().setRam(newPhone.getRam());
                phone.get().setChipset(newPhone.getChipset());
                phone.get().setBatterySize(newPhone.getBatterySize());
                phone.get().setBatteryType(newPhone.getBatteryType());
                phone.get().setReviews(newPhone.getReviews());
                phoneMongo.save(phone.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deletePhoneById(String id) {
        boolean result = true;
        try {
            phoneMongo.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deletePhone(Phone phone) {
        boolean result = true;
        try {
            phoneMongo.delete(phone);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public List<Phone> findRecentPhones() {
        List<Phone> phones = null;
        try {
            Query query = new Query();
            query.with(Sort.by(Sort.Direction.DESC, "releaseYear"));
            phones = mongoOperations.find(query, Phone.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phones;
    }

    public boolean updatePhoneReviewsOldUser(String username) {
        try {
            Query query = Query.query(
                    Criteria.where("reviews.username").is(username));
            Update update = new Update().set("reviews.$.username", "Deleted User");
            mongoOperations.updateMulti(query, update, Phone.class, "phones");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Document findTopRatedBrands(int minReviews, int results) {
        UnwindOperation unwindOperation = unwind("reviews");
        GroupOperation groupOperation = group("$brand").avg("$reviews.rating")
                .as("avgRating").count().as("numReviews");
        MatchOperation matchOperation = match(new Criteria("numReviews").gte(minReviews));
        ProjectionOperation projectionOperation = project().andExpression("_id").as("brand")
                .andExclude("_id").andExpression("numReviews").as("reviews")
                .and(ArithmeticOperators.Round.roundValueOf("avgRating").place(1)).as("rating");
        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "rating",
                "reviews"));
        LimitOperation limitOperation = limit(results);
        Aggregation aggregation = newAggregation(unwindOperation, groupOperation, matchOperation,
                projectionOperation, sortOperation, limitOperation);
        AggregationResults<Phone> result = mongoOperations
                .aggregate(aggregation, "phones", Phone.class);
        return result.getRawResults();
    }

}
*/