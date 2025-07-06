package com.arassec.artivact.application.port.in.account;

import com.arassec.artivact.domain.model.account.Account;

public interface UpdateAccountUseCase {

    Account update(Account account);

    Account updateOwnAccount(String originalUsername, Account account);

}
