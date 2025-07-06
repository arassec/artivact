package com.arassec.artivact.application.port.in.account;

import com.arassec.artivact.domain.model.account.Account;

public interface CreateAccountUseCase {

    Account create(Account account);

}
