# OAuth2

## 1. OAuth2 kya hai?

OAuth 2.0 ek authorization framework hai.

Iska use external providers ke through authorization/login flows ke liye kiya ja sakta hai.

Examples:

```text
Google
GitHub
Facebook
```

---

## 2. OAuth2 Login Flow

```text
User
  ↓
Our Application
  ↓
OAuth2 Provider
  ↓
User Login
  ↓
User Consent
  ↓
Authorization Code
  ↓
Spring Security
  ↓
Provider se User Information
  ↓
Login Success
```

---

## 3. OAuth2 Login URL

Google:

```text
/oauth2/authorization/google
```

GitHub:

```text
/oauth2/authorization/github
```

Facebook:

```text
/oauth2/authorization/facebook
```

---

## 4. Callback URL

Google:

```text
http://localhost:8080/login/oauth2/code/google
```

GitHub:

```text
http://localhost:8080/login/oauth2/code/github
```

Facebook:

```text
http://localhost:8080/login/oauth2/code/facebook
```

Pattern:

```text
/login/oauth2/code/{registrationId}
```

---

## 5. Google Configuration

```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=openid,profile,email
```

---

## 6. GitHub Configuration

```properties
spring.security.oauth2.client.registration.github.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.github.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.github.scope=read:user,user:email
```

---

## 7. Facebook Configuration

```properties
spring.security.oauth2.client.registration.facebook.client-id=YOUR_APP_ID
spring.security.oauth2.client.registration.facebook.client-secret=YOUR_APP_SECRET
spring.security.oauth2.client.registration.facebook.scope=email,public_profile
```

---

## 8. SecurityConfig

```java
.oauth2Login(oauth2 ->
        oauth2.successHandler(
                oAuth2LoginSuccessHandler
        )
)
```

---

## 9. OAuth2 Success Handler

OAuth2 authentication successful hone ke baad custom logic execute kiya ja sakta hai.

Example flow:

```text
OAuth2 Login Success
       ↓
Success Handler
       ↓
User Information
       ↓
Database
       ↓
Generate JWT
       ↓
Generate Refresh Token
```

---

## 10. OAuth2 vs JWT

OAuth2 aur JWT same nahi hain.

```text
OAuth2
→ Authorization framework

JWT
→ Token format
```

Dono ko ek application me saath use kiya ja sakta hai.

---

## 11. Interview Questions

### OAuth2 kya hai?

OAuth2 ek authorization framework hai jo external providers ke saath authorization flows enable karta hai.

### OAuth2 aur JWT me difference?

OAuth2 framework hai, JWT token format hai.

### OAuth2 callback kya hai?

Provider authentication ke baad application ke callback endpoint par response bhejta hai.