package com.arassec.artivact.application.port.in.item;

import com.arassec.artivact.domain.model.exchange.ImportContext;

public interface ImportItemUseCase {

    void importItem(ImportContext importContext, String itemId);

}
