package com.sanosysalvos.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseWrapper<T> {
    private boolean success;
    private T data;
    private String message;
    private Integer total;

    public ResponseWrapper(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public ResponseWrapper(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ResponseWrapper(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public ResponseWrapper(boolean success, T data, int total) {
        this.success = success;
        this.data = data;
        this.total = total;
    }
}
