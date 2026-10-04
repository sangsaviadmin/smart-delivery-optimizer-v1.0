package com.logismart.service;

import com.logismart.model.ZoneDistance;
import com.logismart.repository.ZoneDistanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZoneDistanceService {

    private final ZoneDistanceRepository repository;

    public ZoneDistanceService(ZoneDistanceRepository repository) {
        this.repository = repository;
    }

    public List<ZoneDistance> getAllDistances() {
        return repository.findAll();
    }
}