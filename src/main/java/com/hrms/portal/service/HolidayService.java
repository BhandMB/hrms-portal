package com.hrms.portal.service;

import com.hrms.portal.entity.Holiday;
import com.hrms.portal.repository.HolidayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class HolidayService {

    private final HolidayRepository repository;

    public HolidayService(HolidayRepository repository) {
        this.repository = repository;
    }

    public List<Holiday> getAllHolidays() {
        return repository.findAllByOrderByDateAsc();
    }

    public List<Holiday> getHolidaysForYear(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        return repository.findByDateBetweenOrderByDateAsc(start, end);
    }

    public Holiday getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found with id: " + id));
    }

    @Transactional
    public Holiday create(Holiday holiday) {
        if (holiday.getName() == null || holiday.getName().isBlank()) {
            throw new IllegalArgumentException("Holiday name is required");
        }
        if (holiday.getDate() == null) {
            throw new IllegalArgumentException("Holiday date is required");
        }
        return repository.save(holiday);
    }

    @Transactional
    public Holiday update(Long id, Holiday updated) {
        Holiday existing = getById(id);
        existing.setName(updated.getName());
        existing.setDate(updated.getDate());
        existing.setType(updated.getType());
        existing.setDescription(updated.getDescription());
        existing.setOptional(updated.isOptional());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Holiday not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
