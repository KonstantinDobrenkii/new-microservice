package com.aholddelhaize.iwmsservice.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseData implements Serializable {

    private String errorCode;

    private String errorMessage;

}
