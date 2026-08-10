# Method Security

## 1. Method Security kya hai?

Method Security ka use directly Java methods par authorization rules apply karne ke liye hota hai.

Example:

```java
@PreAuthorize("hasRole('ADMIN')")
```

---

## 2. Enable Method Security

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
}
```

---

## 3. @PreAuthorize

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin")
public String admin() {

    return "Admin endpoint";
}
```

Sirf ADMIN is method ko execute kar sakta hai.

---

## 4. Multiple Roles

```java
@PreAuthorize(
        "hasAnyRole('USER', 'ADMIN')"
)
```

USER aur ADMIN dono allowed hain.

---

## 5. hasAuthority()

```java
@PreAuthorize(
        "hasAuthority('READ_USER')"
)
```

Specific authority check kar sakte hain.

---

## 6. Role vs Authority

Example roles:

```text
ROLE_USER
ROLE_ADMIN
```

Example authorities:

```text
READ_USER
CREATE_USER
UPDATE_USER
DELETE_USER
```

---

## 7. URL Security vs Method Security

URL based:

```java
.requestMatchers("/admin")
.hasRole("ADMIN")
```

Method based:

```java
@PreAuthorize("hasRole('ADMIN')")
```

---

## 8. Method Security Flow

```text
Request
  ↓
Authentication
  ↓
Authorization
  ↓
Method Security
  ↓
@PreAuthorize
  ↓
Method Execute
```

---

## 9. Why Method Security?

Method-level security useful hai jab specific service/controller method ko directly secure karna ho.

---

## 10. Interview Question

### @PreAuthorize kya hai?

`@PreAuthorize` method execute hone se pehle authorization expression evaluate karta hai.

Example:

```java
@PreAuthorize("hasRole('ADMIN')")
```