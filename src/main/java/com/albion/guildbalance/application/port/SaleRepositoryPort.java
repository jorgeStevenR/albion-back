package com.albion.guildbalance.application.port;

import com.albion.guildbalance.domain.entity.Sale;

public interface SaleRepositoryPort {

    Sale save(Sale sale);
}
