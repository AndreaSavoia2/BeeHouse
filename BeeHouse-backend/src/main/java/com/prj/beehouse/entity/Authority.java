package com.prj.beehouse.entity;

import com.prj.beehouse.entity.enumerated.AuthorityName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "authorities")
public class Authority {

    @Id
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false, unique = true)
    private AuthorityName authorityName;

    @Builder.Default
    private boolean authorityDefault = false;

    public Authority(AuthorityName authorityName) {
        this.authorityName = authorityName;
    }
}
