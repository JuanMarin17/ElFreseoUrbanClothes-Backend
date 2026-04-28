package com.apiPreferences.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "preferences")
public class Preferences {
    private Long user_id;
    
    @Column(name = "sports")
    private String sports;

    @Column(name = "colors")
    private String colors;

    @Column(name = "preferences_size")
    private String preferences_size;
    
    @Column(name = "clothing_style")
    private String clothing_style;
}
