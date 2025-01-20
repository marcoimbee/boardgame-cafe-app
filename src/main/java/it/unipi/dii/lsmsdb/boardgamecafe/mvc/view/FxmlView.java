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
        public String getFxmlFile() { return "/viewDetailsBoardgame.fxml"; }
    }, OBJECTBOARDGAME {
        @Override
        public String getTitle() {
            return "Boardgame Preview Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectBoardgame.fxml";
        }
    }, OBJECTPOST {
        @Override
        public String getTitle() {
            return "Post Preview Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectPost.fxml";
        }
    }, OBJECTREVIEW {
        @Override
        public String getTitle() {
            return "Review Preview Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectReview.fxml";
        }
    }, OBJECTCOMMENT {
        @Override
        public String getTitle() {
            return "Comment Preview Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectComment.fxml";
        }
    }, OBJECTCREATECOMMENT {
        @Override
        public String getTitle() {
            return "Create Comment Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectCreateComment.fxml";
        }
    }, OBJECTCREATEPOST {
        @Override
        public String getTitle() {
            return "Create Post Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectCreatePost.fxml";
        }
    }, OBJECTUSER {
        @Override
        public String getTitle() {
            return "User Info Preview Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectUser.fxml";
        }
    }, DETAILS_POST {
        @Override
        public String getTitle() {
            return "Post Details Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewDetailsPostPage.fxml";
        }
    }, INFOMSGUSERS {
        @Override
        public String getTitle() {
            return "Info Users Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectInfoUsersMsg.fxml";
        }
    }, INFOMSGPOSTS {
        @Override
        public String getTitle() {
            return "Info Posts Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectInfoPostsMsg.fxml";
        }
    }, INFOMSGREVIEWS {
        @Override
        public String getTitle() {
            return "Info Reviews Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectInfoReviewsMsg.fxml";
        }
    }, INFOMSGBOARDGAMES {
        @Override
        public String getTitle() {
            return "Info Boardgames Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectInfoBoardgamesMsg.fxml";
        }
    }, SEARCHUSER {
        @Override
        public String getTitle() {
            return "Search Users Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewSearchUserPage.fxml";
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
    }, ACCOUNTINFOPAGE {
        @Override
        public String getTitle() {
            return "Info Account";
        }
        @Override
        public String getFxmlFile() {
            return "/viewAccountInfoPage.fxml";
        }
    }, EDIT_POST {
        @Override
        public String getTitle() {
            return "Post Editing";
        }
        @Override
        public String getFxmlFile() {
            return "/viewEditPostPage.fxml";
        }
    }, EDIT_COMMENT {
        @Override
        public String getTitle() {
            return "Comment Editing";
        }
        @Override
        public String getFxmlFile() {
            return "/viewEditCommentPage.fxml";
        }
    }, OBJECTCREATEBOARDGAME {
        @Override
        public String getTitle() {
            return "Boardgame Creation and Upload";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectCreateBoardgame.fxml";
        }
    }, STATISTICS {
        @Override
        public String getTitle() {
            return "BoardgameCafè_App Statistics Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewStatisticsPage.fxml";
        }
    }, SELECTED_ANALYTIC {
        @Override
        public String getTitle() {
            return "BoardgameCafè_App Analytic Page";
        }
        @Override
        public String getFxmlFile() {
            return "/viewAdminAnalytics.fxml";
        }
    }, EDIT_REVIEW {
        @Override
        public String getTitle() {
            return "Review Editing";
        }
        @Override
        public String getFxmlFile() {
            return "/viewEditReviewPage.fxml";
        }
    }, OBJECTCREATEREVIEW {
        @Override
        public String getTitle() {
            return "Review Creation";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectCreateReview.fxml";
        }
    }, OBJECTREVIEWBLANKBODY {
        @Override
        public String getTitle() {
            return "Review Creation";
        }
        @Override
        public String getFxmlFile() {
            return "/viewObjectReviewBlankBody.fxml";
        }
    };

    public abstract String getTitle();

    public abstract String getFxmlFile();
}
