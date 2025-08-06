package com.nicenpc.swagger;

// 序號 結構 欄位 名稱 長度 資料型態 必要欄位 說明
public record Payload(
    String serialNumber, // 序號
    String structure, // 結構
    String fieldName, // 欄位名稱
    Integer maxLength, // 長度
    String dataType, // 資料型態
    Integer required, // 必要欄位
    String description // 說明
    ) {}
