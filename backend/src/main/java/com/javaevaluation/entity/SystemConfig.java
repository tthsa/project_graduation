package com.javaevaluation.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统配置实体
 */
@Data
public class SystemConfig {
    private Integer id;
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime updatedAt;
}