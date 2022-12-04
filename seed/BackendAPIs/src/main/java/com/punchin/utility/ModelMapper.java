package com.punchin.utility;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class ModelMapper {

    private static org.modelmapper.ModelMapper modelMapper;

   static {
        modelMapper = new org.modelmapper.ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public <T> T map(Object source, Class<T> className) {
        T t = null;
        try {
            if(Objects.nonNull(source)) {
                t = modelMapper.map(source, className);
            }
        } catch (Exception e) {
            log.error("Error in ModelMapperService :: map :", e);
        }
        return t;
    }
}
