package com.enterprise.marketplace.buyerservice.infrastructure.persistence;

import com.enterprise.marketplace.buyerservice.domain.model.Buyer;
import com.enterprise.marketplace.buyerservice.domain.model.BuyerPage;
import com.enterprise.marketplace.buyerservice.domain.model.BuyerSearchCriteria;
import com.enterprise.marketplace.buyerservice.domain.port.BuyerRepository;
import com.enterprise.marketplace.buyerservice.infrastructure.persistence.entity.BuyerEntity;
import com.enterprise.marketplace.buyerservice.infrastructure.persistence.mapper.BuyerPersistenceMapper;
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
public class BuyerRepositoryAdapter implements BuyerRepository {

    private final BuyerJpaRepository buyerJpaRepository;
    private final BuyerPersistenceMapper buyerPersistenceMapper;

    @Override
    public Buyer save(Buyer buyer) {
        BuyerEntity entity = buyer.getId() == null
                ? buyerPersistenceMapper.toEntity(buyer)
                : buyerJpaRepository
                        .findById(buyer.getId())
                        .map(existing -> {
                            buyerPersistenceMapper.updateEntity(existing, buyer);
                            return existing;
                        })
                        .orElseGet(() -> buyerPersistenceMapper.toEntity(buyer));

        return buyerPersistenceMapper.toDomain(buyerJpaRepository.save(entity));
    }

    @Override
    public Optional<Buyer> findById(UUID id) {
        return buyerJpaRepository.findById(id).map(buyerPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Buyer> findByEmail(String email) {
        return buyerJpaRepository.findByEmail(email).map(buyerPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return buyerJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, UUID id) {
        return buyerJpaRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public BuyerPage<Buyer> search(BuyerSearchCriteria criteria, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<BuyerEntity> result = buyerJpaRepository.findAll(BuyerSpecifications.fromCriteria(criteria), pageable);
        return new BuyerPage<>(
                result.getContent().stream().map(buyerPersistenceMapper::toDomain).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    public void deleteById(UUID id) {
        buyerJpaRepository.deleteById(id);
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
