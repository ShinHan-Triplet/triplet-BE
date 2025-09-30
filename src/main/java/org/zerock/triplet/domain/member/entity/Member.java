package org.zerock.triplet.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "members")
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //private String provider;  // "NAVER" <- 네이버밖에 없으니까 굳이 필요 없을 것 같기도 하고?
    private String oauthId;   // 네이버의 유니크 id

    @Column(length = 120)
    private String email;

    @Column(length = 60)
    private String name;

    // 네이버: birthday = "MM-DD", birthyear = "YYYY"
    @Column(length = 12)
    private String birthday;        // ex) 1996. 02. 14

    @Column(length = 512)
    private String profileImage;     // URL

    @Builder.Default
    private String role = "ROLE_USER";
}
