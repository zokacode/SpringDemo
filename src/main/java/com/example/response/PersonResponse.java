package com.example.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class PersonResponse {

    private Long id;
    private String name;
    private String nickname;
    private String sex;
    private String birthday;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant createTime;

}
