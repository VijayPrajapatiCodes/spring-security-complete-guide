# Spring Security Interview Questions & Answers

# 1. Spring Security kya hai?

Spring Security is a framework used for authentication and authorization in Spring applications.

It provides features like:

- Authentication
- Authorization
- Password Encoding
- JWT Security
- Role-Based Access Control
- Method Security
- OAuth2
- Social Login

---

# 2. Authentication aur Authorization me difference?

### Authentication

Authentication verifies:

```text
Who are you?
```

Example:

```text
Username + Password
        ↓
User Identity Verification
```

### Authorization

Authorization verifies:

```text
What are you allowed to access?
```

Example:

```text
USER
 ↓
/admin
 ↓
Access Denied
```

Short answer:

> Authentication verifies the identity of a user, while authorization determines what resources that authenticated user can access.

---

# 3. Spring Security Filter Chain kya hai?

Spring Security Filter Chain is a sequence of security filters through which HTTP requests pass before reaching the controller.

Flow:

```text
Client
  ↓
HTTP Request
  ↓
Security Filter Chain
  ↓
Authentication
  ↓
Authorization
  ↓
Controller
```

---

# 4. SecurityContextHolder kya hai?

`SecurityContextHolder` stores the authentication information of the currently authenticated user.

Example:

```java
SecurityContextHolder
        .getContext()
        .setAuthentication(authentication);
```

---

# 5. UserDetails kya hai?

`UserDetails` is a Spring Security interface that represents user information required for authentication.

It contains information such as:

```text
Username
Password
Authorities
Account status
```

---

# 6. UserDetailsService kya hai?

`UserDetailsService` is used by Spring Security to load user information, usually from the database.

Example:

```java
@Override
public UserDetails loadUserByUsername(
        String username
) {

    return userRepository
            .findByUsername(username)
            .orElseThrow(
                    () -> new UsernameNotFoundException(
                            "User not found"
                    )
            );
}
```

---

# 7. PasswordEncoder kya hai?

`PasswordEncoder` is used to securely encode passwords and verify passwords during authentication.

Example:

```java
@Bean
PasswordEncoder passwordEncoder() {

    return new BCryptPasswordEncoder();
}
```

Password ko plain text me database me store nahi karna chahiye.

---

# 8. AuthenticationManager kya hai?

`AuthenticationManager` is responsible for performing authentication.

Example:

```java
Authentication authentication =
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );
```

Flow:

```text
Username + Password
        ↓
AuthenticationManager
        ↓
UserDetailsService
        ↓
PasswordEncoder
        ↓
Authentication Success
```

---

# 9. JWT kya hai?

JWT stands for:

```text
JSON Web Token
```

JWT is a token format commonly used for stateless authentication.

JWT structure:

```text
Header.Payload.Signature
```

---

# 10. JWT ke 3 parts kya hain?

JWT contains three parts:

```text
1. Header
2. Payload
3. Signature
```

### Header

Contains information like:

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload

Contains claims:

```json
{
  "sub": "vijay",
  "iat": 123456,
  "exp": 123999
}
```

### Signature

Signature is used to verify the integrity of the token.

---

# 11. JWT request me kaise bhejte hain?

JWT is generally sent through the Authorization header.

```http
Authorization: Bearer <ACCESS_TOKEN>
```

Example:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

# 12. JwtAuthenticationFilter ka kya kaam hai?

`JwtAuthenticationFilter` request se JWT extract karta hai, validate karta hai aur authenticated user ko `SecurityContextHolder` me set karta hai.

Flow:

```text
Request
   ↓
Authorization Header
   ↓
Bearer Token
   ↓
Extract JWT
   ↓
Extract Username
   ↓
Load UserDetails
   ↓
Validate Token
   ↓
Create Authentication
   ↓
SecurityContextHolder
   ↓
Controller
```

---

# 13. OncePerRequestFilter kyun use kiya?

`OncePerRequestFilter` ensures that the custom filter is executed once per request.

Example:

```java
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {
}
```

---

# 14. JWT Stateless Authentication kya hai?

Stateless authentication means the server does not maintain a traditional login session.

Client har protected request ke saath JWT send karta hai.

```text
Login
 ↓
JWT Generated
 ↓
Client
 ↓
JWT sent with every request
 ↓
Server validates JWT
```

---

# 15. JWT ka advantage kya hai?

Advantages:

- Stateless authentication
- Scalable
- REST APIs ke liye useful
- Web/mobile applications ke liye useful
- Server-side session dependency kam

---

# 16. JWT ka disadvantage kya hai?

Disadvantages:

- Token leak hone par security risk
- Token revoke karna difficult ho sakta hai
- Secret key secure rakhni hoti hai
- JWT payload unnecessarily large nahi hona chahiye

---

# 17. Access Token kya hai?

Access Token is used to access protected APIs.

Example:

```http
Authorization: Bearer <ACCESS_TOKEN>
```

Usually access token short-lived hota hai.

---

# 18. Refresh Token kya hai?

Refresh Token is a long-lived token used to generate a new access token after the access token expires.

Flow:

```text
Access Token
     ↓
Expired
     ↓
Refresh Token
     ↓
New Access Token
```

User ko dobara username/password enter karne ki zarurat nahi padti.

---

# 19. Access Token vs Refresh Token

| Access Token | Refresh Token |
|---|---|
| Short-lived | Long-lived |
| Protected APIs access karta hai | New access token generate karta hai |
| Frequently expire hota hai | Longer lifetime |
| Every protected request | Refresh endpoint |

---

# 20. Refresh Token database me kyun store karte hain?

Database me refresh token store karne se hum:

- Token validate kar sakte hain
- Expiration check kar sakte hain
- Token revoke kar sakte hain
- Token manage kar sakte hain

Example:

```text
RefreshToken
    ↓
User
    ↓
Expiry Date
```

---

# 21. Refresh Token Expiration kaise check karte hain?

Example:

```java
if (refreshToken.getExpiryDate()
        .isBefore(Instant.now())) {

    refreshTokenRepository.delete(
            refreshToken
    );

    throw new RuntimeException(
            "Refresh token expired"
    );
}
```

---

# 22. Role Based Access Control kya hai?

Role Based Access Control allows or denies access based on the user's role.

Example:

```text
USER
ADMIN
```

Example:

```java
.requestMatchers("/admin")
.hasRole("ADMIN")
```

---

# 23. hasRole() kya karta hai?

`hasRole()` specific role ke user ko access deta hai.

Example:

```java
.hasRole("ADMIN")
```

Sirf ADMIN access kar sakta hai.

---

# 24. hasAnyRole() kya karta hai?

Multiple roles ko allow karne ke liye:

```java
.hasAnyRole("USER", "ADMIN")
```

USER aur ADMIN dono access kar sakte hain.

---

# 25. 401 aur 403 me difference?

## 401 Unauthorized

User properly authenticated nahi hai.

```text
No valid authentication
        ↓
401 Unauthorized
```

## 403 Forbidden

User authenticated hai but required permission nahi hai.

```text
Authenticated USER
        ↓
/admin
        ↓
403 Forbidden
```

Interview me:

> 401 means authentication is missing or invalid, while 403 means the user is authenticated but does not have sufficient permission.

---

# 26. Method Security kya hai?

Method Security allows us to apply authorization directly to Java methods.

Enable:

```java
@EnableMethodSecurity
```

Example:

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser() {

}
```

---

# 27. @PreAuthorize kya karta hai?

`@PreAuthorize` method execute hone se pehle authorization check karta hai.

Example:

```java
@PreAuthorize("hasRole('ADMIN')")
```

Only ADMIN can execute the method.

---

# 28. Role aur Authority me difference?

Role:

```text
ROLE_USER
ROLE_ADMIN
```

Authority:

```text
READ_USER
CREATE_USER
UPDATE_USER
DELETE_USER
```

Role generally high-level access ko represent karta hai, while authority more specific permission ko represent kar sakti hai.

---

# 29. OAuth2 kya hai?

OAuth 2.0 is an authorization framework.

It allows applications to use external providers for authorization.

Examples:

```text
Google
GitHub
Facebook
```

---

# 30. OAuth2 aur JWT me difference?

🔥 Important Interview Question

> OAuth2 is an authorization framework, while JWT is a token format. They are different concepts and can be used together.

```text
OAuth2
   ↓
Authorization Framework

JWT
   ↓
Token Format
```

---

# 31. OAuth2 Login Flow explain karo.

Answer:

> In OAuth2 login, the user is redirected to an external provider such as Google or GitHub. The user authenticates with the provider. After successful authentication, the provider redirects the user back to our application with an authorization code. Spring Security processes the OAuth2 response and retrieves the user's information.

Flow:

```text
Application
    ↓
Google/GitHub/Facebook
    ↓
User Login
    ↓
Consent
    ↓
Authorization Code
    ↓
Spring Security
    ↓
User Information
    ↓
Application
```

---

# 32. Social Login kya hai?

Social Login allows users to login using an existing account from a social provider.

Our project supports:

```text
Google
GitHub
Facebook
```

---

# 33. Google OAuth2 URL kya hai?

```text
/oauth2/authorization/google
```

---

# 34. GitHub OAuth2 URL kya hai?

```text
/oauth2/authorization/github
```

---

# 35. Facebook OAuth2 URL kya hai?

```text
/oauth2/authorization/facebook
```

---

# 36. OAuth2 Callback URL kya hota hai?

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

General pattern:

```text
/login/oauth2/code/{registrationId}
```

---

# 37. Social Login ke baad user database me kaise save hota hai?

Flow:

```text
Social Login
      ↓
OAuth2 Provider
      ↓
User Information
      ↓
Find User in Database
      ↓
User Exists?
   ↙          ↘
 YES          NO
 ↓             ↓
Login       Create User
 ↓             ↓
Generate JWT
      ↓
Refresh Token
      ↓
Login Complete
```

---

# 38. Social Login ke baad JWT kyun generate kar sakte hain?

Answer:

> After successful social authentication, we can generate our application's own JWT so that the same JWT authentication mechanism can be used for our protected APIs.

Flow:

```text
Google/GitHub/Facebook
          ↓
OAuth2 Authentication
          ↓
Our Application
          ↓
Generate JWT
          ↓
Protected APIs
```

---

# 39. OAuth2 Provider ka access token aur hamara JWT same hai?

No.

> The OAuth2 provider's token and our application's JWT are separate tokens with different purposes.

```text
Google Token
     ↓
Google APIs / OAuth2 flow

Our JWT
     ↓
Our Application APIs
```

---

# 40. Complete Authentication Flow explain karo.

🔥 Most Important Project Question

Answer:

> In my project, I implemented both normal authentication and social authentication. For normal login, the username and password are authenticated using AuthenticationManager, UserDetailsService and PasswordEncoder. After successful authentication, I generate an access JWT and refresh token. For protected requests, a custom JWT filter validates the token and sets the authentication in SecurityContextHolder. I also implemented role-based authorization and method-level security. For social login, I integrated OAuth2 with Google, GitHub and Facebook. After successful OAuth2 authentication, the user's information can be stored in the database and our application's authentication flow can be continued.

Complete flow:

```text
                    LOGIN
                      │
          ┌───────────┴───────────┐
          │                       │
   Normal Login              Social Login
          │                       │
 Username + Password       Google/GitHub/Facebook
          │                       │
 AuthenticationManager          OAuth2
          │                       │
          └───────────┬───────────┘
                      ↓
                Authentication
                      ↓
                 Generate JWT
                      ↓
              Access + Refresh
                      ↓
               Protected APIs
                      ↓
          JwtAuthenticationFilter
                      ↓
                Validate JWT
                      ↓
             SecurityContext
                      ↓
             Role Authorization
                      ↓
                 Controller
```

---

# 41. Apne project me kaun-kaun se Spring Security features implement kiye?

Interview answer:

> In my Spring Security project, I implemented database authentication, PasswordEncoder, UserDetailsService, JWT authentication, refresh tokens, role-based authorization, method-level security, OAuth2 login and social login with Google, GitHub and Facebook.

---

# 42. Project me JWT ka complete flow kya hai?

```text
LOGIN
 ↓
AuthenticationManager
 ↓
UserDetailsService
 ↓
PasswordEncoder
 ↓
Authentication Success
 ↓
Generate Access Token
 ↓
Generate Refresh Token
 ↓
Return Tokens
```

Protected request:

```text
Client
 ↓
Authorization: Bearer JWT
 ↓
JwtAuthenticationFilter
 ↓
Extract Token
 ↓
Validate Token
 ↓
Load UserDetails
 ↓
Create Authentication
 ↓
SecurityContextHolder
 ↓
Authorization
 ↓
Controller
```

---

# 43. Access Token expire ho jaye to kya hoga?

```text
Access Token Expired
        ↓
Client sends Refresh Token
        ↓
Refresh Token Repository
        ↓
Token Found?
        ↓
Expiration Check
        ↓
Generate New Access Token
        ↓
Return New Access Token
```

---

# 44. User logout kaise implement kar sakte hain?

JWT stateless hone ki wajah se access token ko server-side session ki tarah directly destroy nahi kiya ja sakta.

Refresh token ko revoke/delete kar sakte hain.

Example:

```java
refreshTokenRepository.deleteByToken(
        request.getRefreshToken()
);
```

Flow:

```text
Logout
 ↓
Refresh Token Delete
 ↓
Refresh Token Invalid
```

---

# 45. Interview me agar pooche "Spring Security project me tumne kya banaya?"

Ye directly bolo:

> I built a Spring Boot Security application where I implemented database-based authentication using Spring Security. I used UserDetailsService to load users, BCrypt PasswordEncoder for password hashing, AuthenticationManager for authentication, JWT for stateless authentication and refresh tokens for token renewal. I implemented role-based authorization and method-level security using @PreAuthorize. I also integrated OAuth2 social login with Google, GitHub and Facebook. For protected APIs, I created a custom JwtAuthenticationFilter that validates the JWT and sets the authenticated user in SecurityContextHolder.

---

# 🔥 TOP 15 QUESTIONS TO PREPARE

1. What is Spring Security?
2. Authentication vs Authorization?
3. What is Security Filter Chain?
4. What is UserDetails?
5. What is UserDetailsService?
6. What is PasswordEncoder?
7. What is AuthenticationManager?
8. What is JWT?
9. JWT structure?
10. Access Token vs Refresh Token?
11. 401 vs 403?
12. What is Role-Based Authorization?
13. What is Method Security?
14. OAuth2 vs JWT?
15. Explain your complete Spring Security project.

---

# ⭐ One-Line Revision

```text
Authentication
→ Who are you?

Authorization
→ What can you access?

UserDetailsService
→ Loads user

PasswordEncoder
→ Secures password

AuthenticationManager
→ Performs authentication

JWT
→ Stateless authentication token

JwtAuthenticationFilter
→ Validates JWT

SecurityContextHolder
→ Stores current authentication

Refresh Token
→ Generates new access token

Role
→ Controls access based on role

@PreAuthorize
→ Method-level authorization

OAuth2
→ Authorization framework

Social Login
→ Google/GitHub/Facebook login

401
→ Authentication problem

403
→ Authorization problem
```

# 🚀 Final Architecture

```text
                    SPRING SECURITY
                          │
        ┌─────────────────┴─────────────────┐
        │                                   │
   Authentication                      Authorization
        │                                   │
        ↓                                   ↓
Database / OAuth2                    Roles / Authorities
        │                                   │
        ↓                                   ↓
AuthenticationManager               hasRole()
        │                            hasAnyRole()
        ↓                            @PreAuthorize
     JWT
        │
        ├───────────────┐
        ↓               ↓
 Access Token      Refresh Token
        │               │
        ↓               ↓
 Protected APIs    New Access Token
        │
        ↓
JwtAuthenticationFilter
        │
        ↓
SecurityContextHolder
        │
        ↓
Controller
```