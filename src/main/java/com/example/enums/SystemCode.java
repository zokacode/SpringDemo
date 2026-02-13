package com.example.enums;

import lombok.Getter;

@Getter
public enum SystemCode {

    // 成功
    SUCCESS("0000", "成功"),
    
    // 參數錯誤
    PARAM_ERROR("4000", "參數錯誤"),
    VALIDATION_ERROR("4001", "資料驗證失敗"),
    
    // 業務錯誤
    DATA_NOT_FOUND("4040", "查無資料"),
    DUPLICATE_DATA("4090", "資料重複"),
    
    // 系統錯誤
    SYSTEM_ERROR("5000", "系統錯誤"),
    DATABASE_ERROR("5001", "資料庫錯誤"),
    
    // 未知系統錯誤
    UNKNOWN_ERROR("9999", "未知系統錯誤");

    // 狀態碼
    private final String code;
    // 訊息
    private final String message;

    private SystemCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
