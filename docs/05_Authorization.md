# 🔐 05 — Authorization

## 1. What is Authorization?

Authorization is the process of determining whether an authenticated user has permission to access a resource or perform an operation.

Simple meaning:

> Authorization = "Tumhe kya access karne ki permission hai?"

Authentication:

```text
Who are you?
```

Authorization:

```text
What are you allowed to access?
```

---

# 2. Authentication vs Authorization

## Authentication

Authentication verifies the identity of a user.

```text
Username + Password
        ↓
Identity Verification
        ↓
Authentication ✅
```

## Authorization

Authorization checks the permissions of an authenticated user.

```text
Authenticated User
        ↓
Role / Authority Check
        ↓
Access Allowed / Denied
```

Simple:

```text
Authentication = Identity
Authorization  = Permission
```

---

# 3. Real-Life Example

Suppose a person enters an office.

First:

```text
ID Card
   ↓
Identity Verification
   ↓
Authentication
```

Then:

```text
Authenticated Person
        ↓
Which rooms can they enter?
        ↓
Authorization
```

Therefore:

```text
Authentication
    ↓
Who are you?

Authorization
    ↓
What can you access?
```

---

# 4. Authorization Flow

Complete security flow:

```text
HTTP Request
      ↓
Authentication
      ↓
Authenticated User
      ↓
Roles / Authorities
      ↓
Authorization Check
      ↓
   ┌──┴──┐
   ↓     ↓
Allowed Denied
   ↓     ↓
Controller 403
```

---

# 5. 401 vs 403

This is very important.

## 401 Unauthorized

Generally means that authentication is required or has not been successfully established.

Example:

```text
/user
   ↓
User not authenticated
   ↓
Authentication Required
```

## 403 Forbidden

User is authenticated but does not have sufficient permission.

Example:

```text
vijay
   ↓
ROLE_USER
   ↓
/admin
   ↓
ROLE_ADMIN required
   ↓
403 Forbidden
```

Remember:

```text
401 → Authentication problem
403 → Authorization problem
```

---

# 6. Roles

Roles are commonly used to represent groups of permissions.

Examples:

```text
ROLE_USER
ROLE_ADMIN
ROLE_MANAGER
```

In Spring Security:

```java
.roles("USER")
```

generally results in:

```text
ROLE_USER
```

Similarly:

```java
.roles("ADMIN")
```

generally results in:

```text
ROLE_ADMIN
```

---

# 7. Our Current Users

Our project has two users.

## User

```text
Username: vijay
Password: 1234
Role: ROLE_USER
```

## Admin

```text
Username: vp
Password: 123
Role: ROLE_ADMIN
```

---

# 8. `hasRole()`

`hasRole()` is used to restrict access to users having a particular role.

Example:

```java
.requestMatchers("/admin")
.hasRole("ADMIN")
```

Meaning:

```text
/admin
   ↓
ROLE_ADMIN required
```

Flow:

```text
/admin
   ↓
Has ROLE_ADMIN?
   ├── YES → Allow ✅
   └── NO  → 403 ❌
```

---

# 9. `hasAnyRole()`

`hasAnyRole()` allows access if the user has at least one of the specified roles.

Example:

```java
.requestMatchers("/user")
.hasAnyRole("USER", "ADMIN")
```

Meaning:

```text
ROLE_USER OR ROLE_ADMIN
```

Both users can access:

```text
vijay → ROLE_USER  → ✅
vp    → ROLE_ADMIN → ✅
```

---

# 10. `hasRole()` vs `hasAnyRole()`

### `hasRole()`

One specific role:

```java
.hasRole("ADMIN")
```

Meaning:

```text
ROLE_ADMIN required
```

### `hasAnyRole()`

Any one of multiple roles:

```java
.hasAnyRole("USER", "ADMIN")
```

Meaning:

```text
ROLE_USER
      OR
ROLE_ADMIN
```

---

# 11. `ROLE_` Prefix

When we write:

```java
.roles("ADMIN")
```

Spring Security generally treats the role as:

```text
ROLE_ADMIN
```

Therefore:

```java
.hasRole("ADMIN")
```

is the normal matching syntax.

Avoid unnecessarily writing:

```java
.hasRole("ROLE_ADMIN")
```

because `hasRole()` handles the standard `ROLE_` prefix.

---

# 12. Authorities

Authorities represent permissions or granted authorities.

Examples:

```text
USER_READ
USER_CREATE
USER_UPDATE
USER_DELETE
REPORT_READ
REPORT_WRITE
```

A role can represent a broader access category, while authorities can represent more granular permissions.

Example:

```text
ROLE_ADMIN
```

could conceptually have permissions such as:

```text
USER_READ
USER_CREATE
USER_UPDATE
USER_DELETE
```

---

# 13. `hasAuthority()`

`hasAuthority()` checks for a specific authority.

Example:

```java
.requestMatchers("/reports")
.hasAuthority("REPORT_READ")
```

Meaning:

```text
REPORT_READ authority required
```

If user has:

```text
REPORT_READ
```

then:

```text
/reports → ✅
```

If user only has:

```text
REPORT_WRITE
```

then:

```text
/reports → ❌
```

---

# 14. `hasAnyAuthority()`

`hasAnyAuthority()` allows access when the user has at least one of the specified authorities.

Example:

```java
.requestMatchers("/reports")
.hasAnyAuthority(
        "REPORT_READ",
        "REPORT_ADMIN"
)
```

Meaning:

```text
REPORT_READ
      OR
REPORT_ADMIN
```

---

# 15. Role vs Authority

Simple difference:

```text
Role
 ↓
High-level access group
```

```text
Authority
 ↓
Specific permission
```

Example:

```text
ROLE_ADMIN
```

versus:

```text
USER_READ
USER_CREATE
USER_UPDATE
USER_DELETE
```

Roles are useful for broad access control.

Authorities are useful for fine-grained permissions.

---

# 16. `permitAll()`

`permitAll()` allows access without requiring authentication.

Example:

```java
.requestMatchers("/hello")
.permitAll()
```

Meaning:

```text
/hello
   ↓
Everyone can access
```

No role is required.

---

# 17. `authenticated()`

`authenticated()` requires the user to be authenticated.

Example:

```java
.anyRequest()
.authenticated()
```

Meaning:

```text
Any authenticated user → Access
```

It does not require a particular role.

Example:

```text
ROLE_USER
   ↓
Authenticated
   ↓
Allowed

ROLE_ADMIN
   ↓
Authenticated
   ↓
Allowed
```

---

# 18. `denyAll()`

`denyAll()` explicitly denies access.

Example:

```java
.requestMatchers("/blocked")
.denyAll()
```

Meaning:

```text
/blocked
   ↓
Everyone → ❌
```

---

# 19. Our Authorization Configuration

Our current configuration:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

    http
            .addFilterBefore(
                    new MyFilter(),
                    UsernamePasswordAuthenticationFilter.class
            )
            .authorizeHttpRequests(auth -> auth

                    .requestMatchers("/hello")
                    .permitAll()

                    .requestMatchers("/admin")
                    .hasRole("ADMIN")

                    .requestMatchers("/user")
                    .hasAnyRole("USER", "ADMIN")

                    .anyRequest()
                    .authenticated()
            )
            .formLogin(form -> form.permitAll());

    return http.build();
}
```

---

# 20. `/hello` Authorization

Rule:

```java
.requestMatchers("/hello")
.permitAll()
```

Meaning:

```text
Authentication required → No
Role required → No
```

Therefore:

```text
Logged In User → ✅
Not Logged In User → ✅
```

---

# 21. `/user` Authorization

Rule:

```java
.requestMatchers("/user")
.hasAnyRole("USER", "ADMIN")
```

Meaning:

```text
ROLE_USER  → ✅
ROLE_ADMIN → ✅
```

Therefore:

```text
vijay → ROLE_USER  → ✅
vp    → ROLE_ADMIN → ✅
```

---

# 22. `/admin` Authorization

Rule:

```java
.requestMatchers("/admin")
.hasRole("ADMIN")
```

Meaning:

```text
ROLE_ADMIN → ✅
ROLE_USER  → ❌
```

Therefore:

```text
vijay → ROLE_USER  → ❌
vp    → ROLE_ADMIN → ✅
```

---

# 23. Practical Test — Vijay

Login:

```text
Username: vijay
Password: 1234
```

Vijay has:

```text
ROLE_USER
```

## `/user`

```text
/user
   ↓
ROLE_USER required
   ↓
ROLE_USER exists
   ↓
Access ✅
```

## `/admin`

```text
/admin
   ↓
ROLE_ADMIN required
   ↓
Vijay has ROLE_USER
   ↓
Access Denied ❌
   ↓
403 Forbidden
```

---

# 24. Practical Test — Admin

Login:

```text
Username: vp
Password: 123
```

Admin has:

```text
ROLE_ADMIN
```

## `/admin`

```text
/admin
   ↓
ROLE_ADMIN required
   ↓
ROLE_ADMIN exists
   ↓
Access ✅
```

## `/user`

```text
/user
   ↓
ROLE_USER OR ROLE_ADMIN
   ↓
ROLE_ADMIN exists
   ↓
Access ✅
```

---

# 25. Authorization Test Matrix

| User | Role | `/hello` | `/user` | `/admin` |
|---|---|---|---|---|
| Not Logged In | — | ✅ | ❌ | ❌ |
| vijay | ROLE_USER | ✅ | ✅ | ❌ |
| vp | ROLE_ADMIN | ✅ | ✅ | ✅ |

This table represents the basic role-based authorization model.

---

# 26. Authorization Rule Order

Authorization rules are evaluated according to the configured request matcher order.

Example:

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/admin/**")
        .hasRole("ADMIN")

        .requestMatchers("/public/**")
        .permitAll()

        .anyRequest()
        .authenticated()
)
```

General principle:

```text
Specific Rules
      ↓
General Rules
      ↓
Fallback Rule
```

Usually:

```text
Specific Matcher
        ↓
Any Request / General Matcher
```

---

# 27. URL-Based Authorization

Authorization can be applied based on URLs.

Example:

```text
/admin/**
   ↓
ADMIN

/user/**
   ↓
USER or ADMIN

/public/**
   ↓
Everyone
```

Example:

```java
.requestMatchers("/admin/**")
.hasRole("ADMIN")

.requestMatchers("/user/**")
.hasAnyRole("USER", "ADMIN")

.requestMatchers("/public/**")
.permitAll()
```

---

# 28. HTTP Method Based Authorization

Authorization can also be based on HTTP methods.

Example:

```text
GET /products
   ↓
USER allowed

POST /products
   ↓
ADMIN only

DELETE /products/{id}
   ↓
ADMIN only
```

Conceptually:

```java
.requestMatchers(HttpMethod.GET, "/products")
.hasAnyRole("USER", "ADMIN")

.requestMatchers(HttpMethod.POST, "/products")
.hasRole("ADMIN")

.requestMatchers(HttpMethod.DELETE, "/products/**")
.hasRole("ADMIN")
```

This allows different permissions for different operations.

---

# 29. E-Commerce Example

Suppose an e-commerce application has:

```text
GET    /products
POST   /products
PUT    /products/{id}
DELETE /products/{id}
```

Authorization could be:

```text
GET /products
    ↓
USER + ADMIN

POST /products
    ↓
ADMIN

PUT /products/{id}
    ↓
ADMIN

DELETE /products/{id}
    ↓
ADMIN
```

This is authorization.

---

# 30. Authentication + Authorization Together

Example:

```text
Vijay Login
    ↓
Authentication
    ↓
SUCCESS
    ↓
ROLE_USER
    ↓
Request /admin
    ↓
Authorization
    ↓
ROLE_ADMIN required
    ↓
ROLE_USER != ROLE_ADMIN
    ↓
403 Forbidden
```

Admin:

```text
Admin Login
    ↓
Authentication
    ↓
SUCCESS
    ↓
ROLE_ADMIN
    ↓
Request /admin
    ↓
Authorization
    ↓
ROLE_ADMIN required
    ↓
Match ✅
    ↓
Controller
```

---

# 31. Complete Security Flow

```text
                    HTTP REQUEST
                         ↓
                Security Filter Chain
                         ↓
                  Authentication
                         ↓
                Who is the user?
                         ↓
                 Authentication OK
                         ↓
              Roles / Authorities
                         ↓
                  Authorization
                         ↓
              What can user access?
                         ↓
                 ┌───────┴───────┐
                 ↓               ↓
              Allowed          Denied
                 ↓               ↓
             Controller          403
```

---

# 32. Role-Based Authorization

Example roles:

```text
ROLE_USER
ROLE_ADMIN
ROLE_MANAGER
```

Possible rules:

```text
/user/**
   ↓
USER or ADMIN

/admin/**
   ↓
ADMIN

/manager/**
   ↓
MANAGER
```

---

# 33. Permission-Based Authorization

Fine-grained permissions can be represented as authorities.

Examples:

```text
USER_READ
USER_CREATE
USER_UPDATE
USER_DELETE
```

Example:

```text
GET /users
   ↓
USER_READ

POST /users
   ↓
USER_CREATE

PUT /users/{id}
   ↓
USER_UPDATE

DELETE /users/{id}
   ↓
USER_DELETE
```

---

# 34. Important Methods

| Method | Meaning |
|---|---|
| `permitAll()` | Everyone can access |
| `authenticated()` | Any authenticated user |
| `hasRole()` | Requires one specific role |
| `hasAnyRole()` | Requires any one of multiple roles |
| `hasAuthority()` | Requires one specific authority |
| `hasAnyAuthority()` | Requires any one of multiple authorities |
| `denyAll()` | Everyone is denied |

---

# 35. Interview Questions

## Q1. What is Authorization?

Authorization determines whether an authenticated user has permission to access a resource or perform an operation.

---

## Q2. Authentication vs Authorization?

```text
Authentication → Who are you?
Authorization  → What can you access?
```

---

## Q3. What is `hasRole()`?

It restricts access to users having a specific role.

```java
.hasRole("ADMIN")
```

---

## Q4. What is `hasAnyRole()`?

It allows access if the user has at least one of the specified roles.

```java
.hasAnyRole("USER", "ADMIN")
```

---

## Q5. What is `hasAuthority()`?

It checks for a specific authority/permission.

```java
.hasAuthority("USER_READ")
```

---

## Q6. What is `hasAnyAuthority()`?

It allows access when the user has at least one of the specified authorities.

---

## Q7. What is `permitAll()`?

It allows access without requiring authentication.

---

## Q8. What is `authenticated()`?

It requires the user to be authenticated but does not require a specific role.

---

## Q9. What is `denyAll()`?

It denies access to everyone for the matching request.

---

## Q10. What does 403 Forbidden mean?

It generally means the user is authenticated but does not have sufficient permissions.

---

## Q11. What does 401 mean?

It generally means authentication is required or authentication has not been successfully established.

---

## Q12. What is the difference between Role and Authority?

```text
Role
 ↓
High-level access group

Authority
 ↓
Specific permission
```

---

# 36. Key Takeaways

```text
Authentication
      ↓
Who are you?
```

```text
Authorization
      ↓
What can you access?
```

```text
hasRole()
      ↓
Specific role
```

```text
hasAnyRole()
      ↓
Any one of multiple roles
```

```text
hasAuthority()
      ↓
Specific permission
```

```text
hasAnyAuthority()
      ↓
Any one of multiple permissions
```

```text
permitAll()
      ↓
Everyone allowed
```

```text
authenticated()
      ↓
Any authenticated user
```

```text
denyAll()
      ↓
Everyone denied
```

---

# 37. Final Mental Model

```text
                    REQUEST
                       ↓
                Authentication
                       ↓
                 Who are you?
                       ↓
                  Authenticated
                       ↓
              Roles / Authorities
                       ↓
                 Authorization
                       ↓
             What can you access?
                       ↓
                ┌──────┴──────┐
                ↓             ↓
             Allowed        Denied
                ↓             ↓
           Controller         403
```

---

# 38. One-Line Revision

> Authorization determines what an authenticated user is allowed to access based on roles, authorities, permissions, and configured security rules.
