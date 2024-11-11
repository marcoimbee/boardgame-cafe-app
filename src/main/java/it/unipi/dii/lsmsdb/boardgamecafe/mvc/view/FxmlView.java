package it.unipi.dii.lsmsdb.boardgamecafe.mvc.view;

public enum FxmlView {

    WELCOMEPAGE {
        @Override
        public String getTitle() {
            return "Welcome Page of BoardGameCafè App";
        }

        @Override
        public String getFxmlFile() {
            return "/viewWelcomePage.fxml";
        }
    }, LOGIN {
        @Override
        public String getTitle() {
            return "LOG-IN Page";
        }

        @Override
        public String getFxmlFile() { return "/viewLogin.fxml"; }
    }, SIGNUP {
        @Override
        public String getTitle() {
            return "Sign-Up Page";
        }

        @Override
        public String getFxmlFile() {
            return "/viewSignUp.fxml";
        }
    }, GUESTBOARDGAMES {
        @Override
        public String getTitle() {
            return "Guest-Boardgames Page";
        }

        @Override
        public String getFxmlFile() {
            return "/viewGuestBoardgamesPage.fxml";
        }
    }, GUESTPOSTS {
        @Override
        public String getTitle() {
            return "Guest-Boardgames's Posts Page";
        }

        @Override
        public String getFxmlFile() {
            return "/viewGuestPostPage.fxml";
        }
    }, REGUSERPOSTS {
        @Override
        public String getTitle() {
            return "Registered User-Boardgames's Posts Page";
        }

        @Override
        public String getFxmlFile() {
            return "/viewRegUserPostPage.fxml";
        }
    }, REGUSERBOARDGAMES {
        @Override
        public String getTitle() {
            return "Registered User-Boardgames Page";
        }

        @Override
        public String getFxmlFile() {
            return "/viewRegUserBoardgamesPage.fxml";
        }
    }, USERPROFILEPAGE {
        @Override
        public String getTitle() {
            return "User HomePage - BoardGameCafè App";
        }

        @Override
        public String getFxmlFile() {
            return "/viewRegUserProfilePage.fxml";
        }
    }, BOARDGAME_DETAILS {
        @Override
        public String getTitle() {
            return "Boardgame Details Page";
        }

        @Override
        public String getFxmlFile() { return "/viewBoardgameDetails.fxml"; }
    }, OBJECTBOARDGAME {
        @Override
        public String getTitle() {
            return "Boardgame Preview";
        }

        @Override
        public String getFxmlFile() {
            return "/viewObjectBoardgame.fxml";
        }
    }, OBJECTPOST {
        @Override
        public String getTitle() {
            return "Post Preview";
        }

        @Override
        public String getFxmlFile() {
            return "/viewObjectPost.fxml";
        }
    }, OBJECTREVIEW {
        @Override
        public String getTitle() {
            return "Review Preview";
        }

        @Override
        public String getFxmlFile() {
            return "/viewObjectReview.fxml";
        }
    }, OBJECTCOMMENT {
        @Override
        public String getTitle() {
            return "Comment Preview";
        }

        @Override
        public String getFxmlFile() {
            return "/viewObjectComment.fxml";
        }
    }, OBJECTCREATECOMMENT {
        @Override
        public String getTitle() {
            return "Create Comment";
        }

        @Override
        public String getFxmlFile() {
            return "/viewObjectCreateComment.fxml";
        }
    }, OBJECTCREATEPOST {
        @Override
        public String getTitle() {
            return "Create Post";
        }

        @Override
        public String getFxmlFile() {
            return "/viewObjectCreatePost.fxml";
        }
    }, DETAILS_POST {
        @Override
        public String getTitle() {
            return "Post Details";
        }

        @Override
        public String getFxmlFile() {
            return "/viewDetailsPostPage.fxml";
        }
    }, INFOMSGPOSTS {
        @Override
        public String getTitle() {
            return "Info Posts";
        }

        @Override
        public String getFxmlFile() {
            return "/viewObjectInfoPostsMsg.fxml";
        }
    }, INFOMSGREVIEWS {
        @Override
        public String getTitle() {
            return "Info Reviews";
        }

        @Override
        public String getFxmlFile() {
            return "/viewObjectInfoReviewsMsg.fxml";
        }
    }, INFOMSGCOMMENTS {
        @Override
        public String getTitle() {
            return "Info Comments";
        }

        @Override
        public String getFxmlFile() {
            return "/viewObjectInfoCommentsMsg.fxml";
        }
    };

    public abstract String getTitle();
    public abstract String getFxmlFile();

}

