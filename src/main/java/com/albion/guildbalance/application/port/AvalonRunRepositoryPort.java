package com.albion.guildbalance.application.port;

import com.albion.guildbalance.domain.entity.AvalonRun;

import java.util.List;
import java.util.Optional;

public interface AvalonRunRepositoryPort {

    AvalonRun save(AvalonRun avalonRun);

    Optional<AvalonRun> findById(Long id);

    List<AvalonRun> findAll();
}
