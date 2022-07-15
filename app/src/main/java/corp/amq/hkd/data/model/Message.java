package corp.amq.hkd.data.model;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message implements IMessage {

    private final String id;
    private String text;
    private Date createdAt;
    private final MessageUser messageUser;

    public Message(String id, MessageUser messageUser, String text, String createdAt) {
        this.id = id;
        this.text = text;
        this.messageUser = messageUser;

        try {
            this.createdAt = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault()).parse(createdAt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public MessageUser getUser() {
        return this.messageUser;
    }

    public String getStatus() {
        return "Sent";
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

}