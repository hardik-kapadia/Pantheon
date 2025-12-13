package com.pantheon.backend.mapper;

import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.model.Game;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GameMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Game toEntity(ScannedLocalGameDTO dto);

}
