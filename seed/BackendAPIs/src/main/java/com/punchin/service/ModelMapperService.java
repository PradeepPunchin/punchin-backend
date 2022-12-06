package com.punchin.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ModelMapperService {


    private static ModelMapper modelMapper;

    static {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    /**
     * Map Entity to DTO.
     *
     * @param <T>       the generic type
     * @param source    the source
     * @param className the class name
     * @return Mapped object
     */


    public <T> T map(Object source, Class<T> className) {
        T t = null;
        try {
            t = modelMapper.map(source, className);
        } catch (Exception e) {
            log.error("Error in model mapper", e);
        }
        return t;
    }
}
