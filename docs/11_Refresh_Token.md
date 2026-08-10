# Refresh Token

## 1. Refresh Token kya hai?

Refresh Token ek long-lived token hota hai jiska use new Access Token generate karne ke liye kiya jata hai.

Normally:

```text
Access Token
→ Short-lived

Refresh Token
→ Long-lived
```

---

## 2. Why Refresh Token?

Suppose Access Token expire ho gaya:

```text
Access Token Expired
       ↓
Refresh Token
       ↓
New Access Token
```

User ko dobara username/password enter karne ki zarurat nahi padti.

---

## 3. Login Flow

```text
Login
  ↓
Username + Password
  ↓
Authentication
  ↓
Generate Access Token
  ↓
Generate Refresh Token
  ↓
Return both
```

Response:

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "uuid-token"
}
```

---

## 4. RefreshToken Entity

```java
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;
}
```

---

## 5. Create Refresh Token

```java
public RefreshToken createRefreshToken(User user) {

    refreshTokenRepository.deleteByUser(user);

    RefreshToken refreshToken =
            new RefreshToken();

    refreshToken.setUser(user);

    refreshToken.setToken(
            UUID.randomUUID().toString()
    );

    refreshToken.setExpiryDate(
            Instant.now()
                    .plusMillis(refreshTokenDurationMs)
    );

    return refreshTokenRepository.save(
            refreshToken
    );
}
```

---

## 6. Old Refresh Token Delete

```java
refreshTokenRepository.deleteByUser(user);
```

Iska meaning hai:

```text
New Login
   ↓
Old Refresh Token Delete
   ↓
New Refresh Token Create
```

---

## 7. Expiration Check

```java
public RefreshToken verifyExpiration(
        RefreshToken refreshToken
) {

    if (refreshToken.getExpiryDate()
            .isBefore(Instant.now())) {

        refreshTokenRepository.delete(
                refreshToken
        );

        throw new RuntimeException(
                "Refresh token expired"
        );
    }

    return refreshToken;
}
```

---

## 8. Refresh Endpoint

```java
@PostMapping("/refresh")
public AuthResponse refreshToken(
        @RequestBody RefreshTokenRequest request
) {

    RefreshToken refreshToken =
            refreshTokenRepository
                    .findByToken(
                            request.getRefreshToken()
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Refresh token not found"
                            )
                    );

    refreshTokenService
            .verifyExpiration(refreshToken);

    String username =
            refreshToken
                    .getUser()
                    .getUsername();

    String accessToken =
            jwtService.generateToken(username);

    return new AuthResponse(
            accessToken,
            refreshToken.getToken()
    );
}
```

---

## 9. Refresh Token Flow

```text
Client
   ↓
Refresh Token
   ↓
Database
   ↓
Find Token
   ↓
Check Expiration
   ↓
Find User
   ↓
Generate New Access Token
   ↓
Return Response
```

---

## 10. Access Token vs Refresh Token

| Access Token | Refresh Token |
|---|---|
| Short-lived | Long-lived |
| API access | New Access Token |
| JWT | Database-backed token in our implementation |
| Every protected request | Only when Access Token needs renewal |

---

## 11. Important Interview Question

### Refresh Token kyun use karte hain?

Access Token ko short-lived rakhne ke liye aur user ko baar-baar login karne se bachane ke liye Refresh Token use kiya jata hai.