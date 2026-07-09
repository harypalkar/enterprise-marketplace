package com.enterprise.marketplace.pricingservice.infrastructure.persistence;

import com.enterprise.marketplace.pricingservice.domain.model.Pricing;
import com.enterprise.marketplace.pricingservice.domain.model.PricingPage;
import com.enterprise.marketplace.pricingservice.domain.model.PricingSearchCriteria;
import com.enterprise.marketplace.pricingservice.domain.port.PricingRepository;
import com.enterprise.marketplace.pricingservice.infrastructure.persistence.entity.PricingEntity;
import com.enterprise.marketplace.pricingservice.infrastructure.persistence.mapper.PricingPersistenceMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class PricingRepositoryAdapter implements PricingRepository {

    private final PricingJpaRepository pricingJpaRepository;
    private final PricingPersistenceMapper pricingPersistenceMapper;

    @Override
    public Pricing save(Pricing pricing) {
        PricingEntity entity = pricing.getId() == null
                ? pricingPersistenceMapper.toEntity(pricing)
                : pricingJpaRepository
                        .findById(pricing.getId())
                        .map(existing -> {
                            pricingPersistenceMapper.updateEntity(existing, pricing);
                            return existing;
                        })
                        .orElseGet(() -> pricingPersistenceMapper.toEntity(pricing));
        return pricingPersistenceMapper.toDomain(pricingJpaRepository.save(entity));
    }

    @Override
    public Optional<Pricing> findById(UUID id) {
        return pricingJpaRepository.findById(id).map(pricingPersistenceMapper::toDomain);
    }

    @Override
    public PricingPage<Pricing> search(PricingSearchCriteria criteria, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<PricingEntity> result = pricingJpaRepository.findAll(PricingSpecifications.fromCriteria(criteria), pageable);
        return new PricingPage<>(
                result.getContent().stream().map(pricingPersistenceMapper::toDomain).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    public void deleteById(UUID id) {
        pricingJpaRepository.deleteById(id);
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction =
                parts.length > 1 && "asc".equalsIgnoreCase(parts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
