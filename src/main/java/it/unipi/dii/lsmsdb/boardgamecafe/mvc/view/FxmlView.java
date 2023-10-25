package it.unipi.dii.lsmsdb.boardgamecafe.mvc.view;

import java.util.ResourceBundle;

public enum FxmlView {

    UNUSER {
        @Override
        public String getTitle() {
            return "PhoneWorld";
        }

        @Override
        public String getFxmlFile() {
            return "/viewUnregisteredUser.fxml";
        }
    }, AUTORIZATION {
        @Override
        public String getTitle() {
            return "LOGIN/SIGN UP";
        }

        @Override
        public String getFxmlFile() {
            return "/viewAuthorization.fxml";
        }
    }, LOGIN {
        @Override
        public String getTitle() {
            return "LOG IN";
        }

        @Override
        public String getFxmlFile() {
            return "/viewLogin.fxml";
        }
    }, SIGNUP {
        @Override
        public String getTitle() {
            return "SIGN UP";
        }

        @Override
        public String getFxmlFile() {
            return "/viewSignUp.fxml";
        }
    }, USER {
        @Override
        public String getTitle() {
            return "PhoneWorld";
        }

        @Override
        public String getFxmlFile() {
            return "/viewRegisteredUser.fxml";
        }
    }, PROFILE {
        @Override
        public String getTitle() {
            return "YOUR PROFILE";
        }

        @Override
        public String getFxmlFile() {
            return "/viewProfile.fxml";
        }
    }, UPDATE {
        @Override
        public String getTitle() {
            return "UPDATE PROFILE";
        }

        @Override
        public String getFxmlFile() {
            return "/viewUpdate.fxml";
        }
    }, DETAILS_USER {
        @Override
        public String getTitle() {
            return "USER DETAILS";
        }

        @Override
        public String getFxmlFile() {
            return "/viewDetailsUser.fxml";
        }
    }, DETAILS_PHONES {
        @Override
        public String getTitle() {
            return "PHONE DETAILS";
        }

        @Override
        public String getFxmlFile() {
            return "/viewDetailsPhone.fxml";
        }
    }, STATISTISCS {
        @Override
        public String getTitle() {
            return "STATISTICS";
        }

        @Override
        public String getFxmlFile() {
            return "/viewStatistics.fxml";
        }
    }, REVIEW {
        @Override
        public String getTitle() {
            return "REVIEW";
        }

        @Override
        public String getFxmlFile() {
            return "/viewReview.fxml";
        }
    }, FIND_REVIEWS {
        @Override
        public String getTitle() {
            return "FIND REVIEWS";
        }

        @Override
        public String getFxmlFile() {
            return "/viewFindReviews.fxml";
        }
    }, ADD_ADMIN {
        @Override
        public String getTitle() {
            return "ADD ADMIN";
        }

        @Override
        public String getFxmlFile() {
            return "/viewAddAdmin.fxml";
        }
    }, MANAGEMENT_PHONE {
        @Override
        public String getTitle() {
            return "PHONE HANDLER";
        }

        @Override
        public String getFxmlFile() {
            return "/viewManagementPhone.fxml";
        }
    };

    public abstract String getTitle();
    public abstract String getFxmlFile();

    String getStringFromResourceBoundle(String key) {
        return ResourceBundle.getBundle("Bundle").getString(key);
    }
}


