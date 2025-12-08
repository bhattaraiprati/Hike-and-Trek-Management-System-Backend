package com.example.treksathi.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponseDTO<T> {
    private List<T> data;
    private PaginationMetadata pagination;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaginationMetadata {
        private int currentPage;
        private int totalPages;
        private int pageSize;
        private long totalElements;
        private boolean hasNext;
        private boolean hasPrevious;
    }

    public static <T> PaginatedResponseDTO<T> of(Page<T> page) {
        PaginationMetadata metadata = new PaginationMetadata(
                page.getNumber() + 1,
                page.getTotalPages(),
                page.getSize(),
                page.getTotalElements(),
                page.hasNext(),
                page.hasPrevious()
        );
        return new PaginatedResponseDTO<>(page.getContent(), metadata);
    }
}
