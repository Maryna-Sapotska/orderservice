package com.innowise.orderservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageableUtils {

    public static Pageable buildPageable(
            int page,
            int size,
            String sort
    ) {

        String[] sortParams = sort.split(",");

        if (sortParams.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid sort format. Use field,direction"
            );
        }

        Sort.Direction direction;

        try {
            direction = Sort.Direction.fromString(sortParams[1]);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Sort direction must be asc or desc"
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(direction, sortParams[0])
        );
    }
}
