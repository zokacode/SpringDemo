package com.example.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PersonCreateRequest {

    @NotBlank(message = "姓名不能為空")
    @Size(max = 50, message = "姓名長度不能超過50個字元")
    private String name;

    @Size(max = 50, message = "暱稱長度不能超過50個字元")
    private String nickname;

    @Size(max = 10, message = "性別長度不能超過10個字元")
    private String sex;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "生日格式必須為 yyyy-MM-dd")
    private String birthday;

    @Size(max = 200, message = "備註長度不能超過200個字元")
    private String description;

}
