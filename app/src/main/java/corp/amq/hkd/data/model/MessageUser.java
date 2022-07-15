package corp.amq.hkd.data.model;

import com.stfalcon.chatkit.commons.models.IUser;

import corp.amq.hkd.data.firebase.User;

public class MessageUser implements IUser {

    private User user;
    private String uid;

    public MessageUser(String uid, User user) {
        this.user = user;
        this.uid = uid;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getId() {
        return uid;
    }

    @Override
    public String getName() {
        return user.getDisplay_name();
    }

    @Override
    public String getAvatar() {
        return user.getProfile_img_url();
    }

}