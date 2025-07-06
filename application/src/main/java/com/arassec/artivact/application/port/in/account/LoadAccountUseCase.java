package com.arassec.artivact.application.port.in.account;

import com.arassec.artivact.domain.model.account.Account;

import java.util.List;
import java.util.Optional;

public interface LoadAccountUseCase {

    List<Account> loadAll();

    List<Account> loadAllExcept(String username);

    Optional<Account> loadByUsername(String username);

    Optional<Account> loadByApiToken(String apiToken);

}
