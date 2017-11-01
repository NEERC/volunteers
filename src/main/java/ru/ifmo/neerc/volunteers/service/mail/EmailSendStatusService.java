package ru.ifmo.neerc.volunteers.service.mail;

import org.springframework.stereotype.Service;

/**
 * Created by Matvey on 11/1/17.
 */
@Service
public class EmailSendStatusService {

    private boolean sendingMails = true;

    public void stopSendingMails() {
        this.sendingMails = false;
    }

    public void resumeSendingMails() {
        this.sendingMails = true;
    }

    public boolean doWeSendEmails() {
        return  this.sendingMails;
    }

}
