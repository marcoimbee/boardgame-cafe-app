package it.unipi.dii.lsmsdb.boardgamecafe.services;

//import it.unipi.dii.lsmsdb.phoneworld.App;
//import it.unipi.dii.lsmsdb.phoneworld.model.Phone;
//import it.unipi.dii.lsmsdb.phoneworld.repository.mongo.PhoneMongo;
//import it.unipi.dii.lsmsdb.phoneworld.repository.mongo.ReviewMongo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BoardgameService {

    @Autowired
    private BoardgameMongoDB boardgameMongo;
    @Autowired
    private ReviewMongoDB reviewMongo;

    private final static Logger logger = LoggerFactory.getLogger(ServicePhone.class);

    public boolean deletePhone(Phone phone) {
        String phoneName = phone.getName();
        String phoneId = phone.getId();
        try {
            if (!phoneMongo.deletePhone(phone)) {
                logger.error("Error in deleting the phone from MongoDB");
                return false;
            }
            if (!App.getInstance().getPhoneNeo4j().deletePhoneRelationships(phoneId)) {
                logger.error("Error in deleting phone's relationships");
                return false;
            }
            if (!App.getInstance().getPhoneNeo4j().deletePhoneOnly(phoneId)) {
                logger.error("Error in deleting the phone from Neo4j");
                return false;
            }
            if (!reviewMongo.deleteReviewByPhoneName(phoneName)) {
                logger.error("Error in deleting the reviews of the phone from the reviews collection");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean updatePhone(Phone phone) {
        try {
            String phoneId = phone.getId();
            String brand = phone.getBrand();
            String picture = phone.getPicture();
            int releaseYear = phone.getReleaseYear();
            if (!phoneMongo.updatePhone(phone.getId(), phone)) {
                logger.error("Error in updating the phone in MongoDB");
                return false;
            }
            if (!App.getInstance().getPhoneNeo4j().updatePhone(phoneId, brand, picture, releaseYear)) {
                logger.error("Error in updating the phone on Neo4j");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean insertPhone(Phone phone, String brand, String name, String picture,
                               int releaseYear) {
        boolean result = true;
        if (!phoneMongo.addPhone(phone)) {
            logger.error("Error in adding the phone to MongoDB");
            return false;
        }
        Optional<Phone> phoneResult = phoneMongo.findPhoneByName(name);
        if (phoneResult.isEmpty()) {
            logger.error("Error in retriving the phone to MongoDB");
            return false;
        }
        String id = phoneResult.get().getId();
        if (!App.getInstance().getPhoneNeo4j().addPhone(id, brand, name, picture, releaseYear)) {
            logger.error("Error in adding the phone to Neo4j");
            if (!phoneMongo.deletePhone(phone)) {
                logger.error("Error in deleting the phone from MongoDB");
            }
            return false;
        }
        return result;
    }

}
