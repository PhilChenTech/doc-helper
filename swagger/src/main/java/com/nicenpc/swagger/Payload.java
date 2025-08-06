package com.nicenpc.swagger;

import java.util.List;

// 序號 結構 欄位 名稱 長度 資料型態 必要欄位 說明
public record Payload(List<PayloadField> fields // 欄位列表
    ) {}
