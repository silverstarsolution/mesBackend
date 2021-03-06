package com.mes.mesBackend.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MapperImpl implements ModelMapper {

    @Autowired
    org.modelmapper.ModelMapper modelMapper;

    public MapperImpl(org.modelmapper.ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public <E, R> E toEntity(R source, Class<? extends E> destinationType) {
        return modelMapper.map(source, destinationType);
    }

    @Override
    public <E, R> List<E> toEntities(List<R> sources, Class<? extends E> destinationType) {
        return sources.stream().map(source -> modelMapper.map(sources, destinationType)).collect(Collectors.toList());
    }

    @Override
    public <E, R> R toResponse(E source, Class<? extends R> destinationType) {
        return modelMapper.map(source, destinationType);
    }

    @Override
    public <E, R> Page<R> toPageResponses(Page<E> sources, Class<? extends R> destinationType) {
        modelMapper.getConfiguration().setSkipNullEnabled(false);
        return sources.map(source -> modelMapper.map(source, destinationType));
    }

    @Override
    public <E, R> List<R> toListResponses(List<E> sources, Class<? extends R> destinationType) {
        return sources.stream().map(source -> modelMapper.map(source, destinationType)).collect(Collectors.toList());
    }
}
