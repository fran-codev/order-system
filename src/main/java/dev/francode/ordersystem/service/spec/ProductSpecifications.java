package dev.francode.ordersystem.service.spec;

import dev.francode.ordersystem.dto.product.ProductFilter;
import dev.francode.ordersystem.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

public class ProductSpecifications {

    public static Specification<Product> filterBy(ProductFilter filter) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();

            if (filter.getName() != null && !filter.getName().trim().isEmpty()) {
                predicates.add(
                        cb.like(cb.lower(root.get("name")), "%" + filter.getName().trim().toLowerCase() + "%")
                );
            }

            if (filter.getMinPrice() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice())
                );
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice())
                );
            }

            if (filter.getCategoryId() != null) {
                predicates.add(
                        cb.equal(root.get("category").get("id"), filter.getCategoryId())
                );
            }

            if (filter.getStatus() != null) {
                predicates.add(
                        cb.equal(root.get("status"), filter.getStatus())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
