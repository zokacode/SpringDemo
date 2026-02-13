package com.example.response;

import com.example.enums.SystemCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseResp<T> {

    private String code = SystemCode.SUCCESS.getCode();
    private String message = SystemCode.SUCCESS.getMessage();
    private T data;

    // 成功 - 帶資料
    public static <T> BaseResp<T> success(T data) {
        return new BaseResp<>(SystemCode.SUCCESS.getCode(), SystemCode.SUCCESS.getMessage(), data);
    }

    // 成功 - 無資料
    public static BaseResp<Void> success() {
        return new BaseResp<>(SystemCode.SUCCESS.getCode(), SystemCode.SUCCESS.getMessage(), null);
    }

    // 失敗
    public static <T> BaseResp<T> error(SystemCode systemCode) {
        return new BaseResp<>(systemCode.getCode(), systemCode.getMessage(), null);
    }

    // 失敗 - 自訂訊息
    public static <T> BaseResp<T> error(SystemCode systemCode, String customMsg) {
        return new BaseResp<>(systemCode.getCode(), customMsg, null);
    }

    // 失敗 - 自訂狀態訊息
    public static <T> BaseResp<T> error(String code, String msg) {
        return new BaseResp<>(code, msg, null);
    }
}
