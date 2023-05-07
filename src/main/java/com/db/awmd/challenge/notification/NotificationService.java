package com.db.awmd.challenge.notification;

import com.db.awmd.challenge.account.domain.Account;

public interface NotificationService {

  void notifyAboutTransfer(Account account, String transferDescription);
}
