package com.enterprise.marketplace.sellerservice.infrastructure.persistence;

import com.enterprise.marketplace.sellerservice.domain.model.Seller;
import com.enterprise.marketplace.sellerservice.domain.model.SellerPage;
import com.enterprise.marketplace.sellerservice.domain.model.SellerSearchCriteria;
import com.enterprise.marketplace.sellerservice.domain.port.SellerRepository;
import com.enterprise.marketplace.sellerservice.infrastructure.persistence.entity.SellerEntity;
import com.enterprise.marketplace.sellerservice.infrastructure.persistence.mapper.SellerPersistenceMapper;
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
public class SellerRepositoryAdapter implements SellerRepository {

    private final SellerJpaRepository sellerJpaRepository;
    private final SellerPersistenceMapper sellerPersistenceMapper;

    @Override
    public Seller save(Seller seller) {
        SellerEntity entity = seller.getId() == null
                ? sellerPersistenceMapper.toEntity(seller)
                : sellerJpaRepository
                        .findById(seller.getId())
                        .map(existing -> {
                            sellerPersistenceMapper.updateEntity(existing, seller);
                            return existing;
                        })
                        .orElseGet(() -> sellerPersistenceMapper.toEntity(seller));

        return sellerPersistenceMapper.toDomain(sellerJpaRepository.save(entity));
    }

    @Override
    public Optional<Seller> findById(UUID id) {
        return sellerJpaRepository.findById(id).map(sellerPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Seller> findByGstin(String gstin) {
        return sellerJpaRepository.findByGstin(gstin).map(sellerPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByGstin(String gstin) {
        return sellerJpaRepository.existsByGstin(gstin);
    }

    @Override
    public boolean existsByGstinAndIdNot(String gstin, UUID id) {
        return sellerJpaRepository.existsByGstinAndIdNot(gstin, id);
    }

    @Override
    public SellerPage<Seller> search(SellerSearchCriteria criteria, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<SellerEntity> result = sellerJpaRepository.findAll(SellerSpecifications.fromCriteria(criteria), pageable);

        return new SellerPage<>(
                result.getContent().stream().map(sellerPersistenceMapper::toDomain).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    public void deleteById(UUID id) {
        sellerJpaRepository.deleteById(id);
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
