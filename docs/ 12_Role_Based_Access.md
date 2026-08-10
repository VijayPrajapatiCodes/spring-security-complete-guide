# Role Based Access Control

## 1. Authorization kya hai?

Authorization ka meaning hai:

```text
User ko kya access karne ki permission hai?
```

Authentication:

```text
Who are you?
```

Authorization:

```text
What are you allowed to access?
```

---

## 2. Example

```text
USER
ADMIN
```

Example:

```text
USER → /user
ADMIN → /admin
```

USER ko `/admin` access karne par:

```text
403 Forbidden
```

---

## 3. Role Database

Example:

```text
username | role
----------------
vijay    | USER
admin    | ADMIN
```

---

## 4. hasRole()

```java
.requestMatchers("/admin")
.hasRole("ADMIN")
```

Sirf ADMIN access kar sakta hai.

---

## 5. hasAnyRole()

```java
.requestMatchers("/user")
.hasAnyRole("USER", "ADMIN")
```

USER aur ADMIN dono access kar sakte hain.

---

## 6. Example SecurityConfig

```java
.authorizeHttpRequests(auth -> auth

        .requestMatchers("/api/users/login")
        .permitAll()

        .requestMatchers("/api/users/register")
        .permitAll()

        .requestMatchers("/api/users/refresh")
        .permitAll()

        .requestMatchers("/admin")
        .hasRole("ADMIN")

        .requestMatchers("/user")
        .hasAnyRole("USER", "ADMIN")

        .anyRequest()
        .authenticated()
)
```

---

## 7. Role Flow

```text
Login
  ↓
JWT
  ↓
JwtAuthenticationFilter
  ↓
UserDetails
  ↓
Authorities
  ↓
ROLE_USER / ROLE_ADMIN
  ↓
Authorization
  ↓
Endpoint Access
```

---

## 8. 401 vs 403

### 401 Unauthorized

User authenticated nahi hai.

```text
No valid authentication
       ↓
401
```

### 403 Forbidden

User authenticated hai but permission nahi hai.

```text
USER
 ↓
/admin
 ↓
403 Forbidden
```

---

## 9. Important Interview Questions

### Authentication aur Authorization me difference?

Authentication user ki identity verify karta hai.

Authorization user ki permissions check karta hai.

### hasRole() kya karta hai?

Specific role ko endpoint access deta hai.

### hasAnyRole() kya karta hai?

Multiple allowed roles me se kisi ek role ko access deta hai.