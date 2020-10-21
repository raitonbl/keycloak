package com.raitonbl.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Page<T> implements Serializable {

    @JsonProperty("size")
    private long size;

    @JsonProperty("number")
    private long number;

    @JsonProperty("total_pages")
    private long totalPages;

    @JsonProperty("content")
    private List<T> container;

    @JsonProperty("total_elements")
    private long totalElements;

    private Page() {
    }

    public long getSize() {
        return size;
    }

    public long getNumber() {
        return number;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public List<T> getContainer() {
        return container;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public static <Y> Builder<Y> builder() {
        return new Builder<>();
    }

    public static class Builder<Y> {

        private long size;
        private long number;
        private List<Y> container;
        private long totalElements;

        private Builder() {
        }

        public Builder<Y> setSize(long size) {
            this.size = size;
            return this;
        }

        public Builder<Y> setNumber(long number) {
            this.number = number;
            return this;
        }

        public Builder<Y> setContainer(List<Y> container) {
            this.container = container;
            return this;
        }

        public Builder<Y> setTotalElements(long totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public Page<Y> build() {

            if (this.size < 1) {
                throw new IllegalArgumentException("Size must be greater than ZERO (0)");
            }

            if (this.number < 1) {
                throw new IllegalArgumentException("Number must be greater than ZERO (0)");
            }

            List<Y> container = this.container == null ? Collections.emptyList()
                    : Collections.unmodifiableList(this.container);

            Long totalElements = null;

            if (container.isEmpty()) {
                totalElements = this.totalElements;
            }

            long offset = (number - 1) * size;

            if (totalElements == null && (offset + size) > this.totalElements) {
                totalElements = this.totalElements;
            }

            if (totalElements == null) {
                totalElements = offset + container.size();
            }

            Page<Y> instance = new Page<>();
            instance.container = container;
            instance.size = size;
            instance.number = number;
            instance.totalElements = totalElements;
            instance.totalPages = (int) Math.ceil((double) totalElements / (double) size);

            return instance;
        }


    }


}
