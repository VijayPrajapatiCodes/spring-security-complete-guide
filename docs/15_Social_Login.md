# Social Login

## 1. Social Login kya hai?

Social Login me user apne existing social account ka use karke application me login karta hai.

Examples:

```text
Google
GitHub
Facebook
```

---

## 2. Login Page

Application ke login page par:

```text
Username
Password

[ Login ]

OR

[ Continue with Google ]

[ Continue with GitHub ]

[ Continue with Facebook ]
```

---

## 3. Google Button

```html
<a href="/oauth2/authorization/google">
    Continue with Google
</a>
```

---

## 4. GitHub Button

```html
<a href="/oauth2/authorization/github">
    Continue with GitHub
</a>
```

---

## 5. Facebook Button

```html
<a href="/oauth2/authorization/facebook">
    Continue with Facebook
</a>
```

---

## 6. Social Login Flow

```text
Login Page
    ↓
Google / GitHub / Facebook
    ↓
OAuth2 Provider
    ↓
User Authorization
    ↓
Authorization Code
    ↓
Spring Security
    ↓
Success Handler
    ↓
User Information
    ↓
Database Check
    ↓
Generate JWT
    ↓
Generate Refresh Token
    ↓
Login Complete
```

---

## 7. Existing User

Agar social account ka user database me already exist karta hai:

```text
Social Login
    ↓
Find User
    ↓
User Found
    ↓
Generate JWT
    ↓
Login
```

---

## 8. New User

Agar user database me exist nahi karta:

```text
Social Login
    ↓
Get User Information
    ↓
Create User
    ↓
Save Database
    ↓
Generate JWT
    ↓
Generate Refresh Token
    ↓
Login
```

---

## 9. Social Login + JWT Architecture

```text
                    LOGIN
                      │
          ┌───────────┴───────────┐
          │                       │
  Username/Password          Social Login
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
```

---

## 10. Protected API

Login ke baad client:

```http
GET /api/users/profile
```

Header:

```http
Authorization: Bearer <ACCESS_TOKEN>
```

Flow:

```text
Request
  ↓
JwtAuthenticationFilter
  ↓
Extract JWT
  ↓
Validate JWT
  ↓
Load User
  ↓
SecurityContext
  ↓
Authorization
  ↓
Controller
```

---

## 11. Three Social Providers

Our project:

```text
Google
GitHub
Facebook
```

Links:

```text
/oauth2/authorization/google

/oauth2/authorization/github

/oauth2/authorization/facebook
```

---

## 12. Complete Authentication Architecture

```text
                         CLIENT
                           │
                           ↓
                       LOGIN PAGE
                           │
              ┌────────────┴────────────┐
              │                         │
       Normal Login                Social Login
              │                         │
      Username/Password          Google/GitHub/Facebook
              │                         │
              ↓                         ↓
    AuthenticationManager             OAuth2
              │                         │
              └────────────┬────────────┘
                           ↓
                    Authentication
                           ↓
                     Generate JWT
                           ↓
                  Access + Refresh Token
                           ↓
                    Protected APIs
                           ↓
              JwtAuthenticationFilter
                           ↓
                     Validate JWT
                           ↓
                  SecurityContext
                           ↓
                  Role/Authority Check
                           ↓
                      Controller
```

---

## 13. Final Spring Security Flow

```text
REGISTER
   ↓
DATABASE

LOGIN
   ↓
Authentication
   ↓
JWT + Refresh Token

REQUEST
   ↓
Bearer Token
   ↓
JwtAuthenticationFilter
   ↓
JWT Validation
   ↓
SecurityContext
   ↓
Authorization
   ↓
Controller

ACCESS TOKEN EXPIRED
   ↓
Refresh Token
   ↓
New Access Token

SOCIAL LOGIN
   ↓
Google / GitHub / Facebook
   ↓
OAuth2
   ↓
User Information
   ↓
Database
   ↓
JWT + Refresh Token
   ↓
Protected APIs
```

---

## 14. Important Interview Questions

### Social Login kya hai?

Social Login user ko Google, GitHub ya Facebook jaise external provider ke account se application me authenticate karne deta hai.

### Social Login me OAuth2 ka role kya hai?

OAuth2 external provider ke saath authorization/authentication flow handle karta hai.

### Social Login ke baad JWT kyun generate karte hain?

Application ki protected APIs ko same JWT authentication mechanism se access karne ke liye.

### Kya Google ka access token hamari API ka JWT hota hai?

Nahi.

Provider token aur application ka JWT alag concepts hain.

### Multiple providers kaise add karte hain?

Har provider ke liye:

```text
Client ID
Client Secret
Scopes
Registration
Callback
```

configure kiye jate hain.

---

# Spring Security Complete

```text
01 Introduction
02 Architecture
03 Security Filter Chain
04 Authentication
05 Authorization
06 In-Memory User
07 Database Authentication
08 UserDetailsService
09 PasswordEncoder
10 JWT
11 Refresh Token
12 Role Based Access
13 Method Security
14 OAuth2
15 Social Login
```

🔥 **Ab tumhara Spring Security ka complete practical roadmap cover ho gaya: Basic Authentication → Database → JWT → Refresh Token → Roles → Method Security → OAuth2 → Google/GitHub/Facebook Social Login.**