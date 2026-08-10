# 🔐 04 — Authentication

## 1. What is Authentication?

Authentication is the process of verifying the identity of a user.

Simple meaning:

> Authentication = "Tum kaun ho?"

Example:

```text
Username: vijay
Password: 1234
        ↓
Spring Security
        ↓
Credentials Verification
        ↓
Authentication Success
```

If credentials are valid:

```text
Authentication Successful ✅
```

If credentials are invalid:

```text
Authentication Failed ❌
```

---

# 2. Authentication vs Authorization

This difference is very important.

## Authentication

```text
Who are you?
```

It verifies the identity of the user.

## Authorization

```text
What are you allowed to do?
```

It checks whether the authenticated user has permission to access a resource.

Example:

```text
Vijay
  ↓
Username + Password Verification
  ↓
Authentication ✅
  ↓
ROLE_USER
  ↓
Can access /admin?
  ↓
Authorization
```

Simple:

```text
Authentication = Identity
Authorization  = Permission
```

---

# 3. Real-Life Example

Airport example:

```text
ID Card
   ↓
Identity Verification
   ↓
Authentication
```

After identity verification:

```text
Passenger
   ↓
Which areas can the passenger enter?
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

# 4. Spring Security Authentication Flow

Spring Security authentication involves multiple components.

Simplified flow:

```text
User
 ↓
Username + Password
 ↓
Authentication Filter
 ↓
AuthenticationManager
 ↓
AuthenticationProvider
 ↓
UserDetailsService
 ↓
UserDetails
 ↓
Password Verification
 ↓
Authenticated
 ↓
SecurityContext
```

---

# 5. Main Authentication Components

Important Spring Security authentication components:

```text
Authentication
AuthenticationManager
AuthenticationProvider
UserDetails
UserDetailsService
PasswordEncoder
SecurityContext
SecurityContextHolder
UsernamePasswordAuthenticationToken
```

Basic relationship:

```text
Request
   ↓
Authentication
   ↓
AuthenticationManager
   ↓
AuthenticationProvider
   ↓
UserDetailsService
   ↓
UserDetails
   ↓
Password Verification
   ↓
Authenticated
   ↓
SecurityContext
```

---

# 6. Authentication Object

Spring Security provides:

```java
Authentication
```

as an interface representing authentication information.

Conceptually:

```text
Authentication
      │
      ├── Principal
      ├── Credentials
      └── Authorities
```

Example:

```text
Principal
   ↓
vp

Credentials
   ↓
Password

Authorities
   ↓
ROLE_ADMIN
```

After successful authentication, the `Authentication` object represents the authenticated user.

---

# 7. Principal

`Principal` represents the identity of the authenticated user.

Example:

```text
Principal
   ↓
vp
```

or:

```text
Principal
   ↓
vijay
```

The exact object representing the principal depends on the authentication mechanism.

---

# 8. Credentials

Credentials are information used to authenticate the user.

In username/password authentication:

```text
Username
Password
```

can be used as authentication credentials.

Important:

> Passwords and other sensitive credentials should never be unnecessarily exposed in application responses or logs.

---

# 9. Authorities

Authorities represent permissions/roles associated with the authenticated user.

Examples:

```text
ROLE_USER
ROLE_ADMIN
```

Example:

```text
Authentication
      ↓
Authorities
      ↓
ROLE_ADMIN
```

Authorization can use these authorities to decide whether a user can access a resource.

---

# 10. AuthenticationManager

`AuthenticationManager` is the central interface responsible for authentication.

Important method:

```java
Authentication authenticate(Authentication authentication)
```

Conceptually:

```text
Username + Password
        ↓
Authentication
        ↓
AuthenticationManager
        ↓
Authentication Result
```

If successful:

```text
Authenticated Authentication
```

If unsuccessful:

```text
AuthenticationException
```

---

# 11. Does AuthenticationManager Perform Authentication Directly?

Generally, `AuthenticationManager` coordinates/delegates authentication to suitable `AuthenticationProvider` implementations.

Conceptually:

```text
AuthenticationManager
        ↓
AuthenticationProvider
        ↓
Actual Authentication
```

Remember:

```text
AuthenticationManager
    ↓
Coordinates authentication

AuthenticationProvider
    ↓
Performs authentication for a particular mechanism
```

---

# 12. AuthenticationProvider

`AuthenticationProvider` is an interface used to perform authentication for a particular authentication mechanism.

Important methods:

```java
Authentication authenticate(Authentication authentication)
```

and:

```java
boolean supports(Class<?> authentication)
```

Conceptually:

```text
AuthenticationManager
        ↓
AuthenticationProvider
        ↓
Authenticate User
```

---

# 13. DaoAuthenticationProvider

For username/password authentication, Spring Security commonly uses:

```text
DaoAuthenticationProvider
```

It works with user details loaded through `UserDetailsService` and password verification.

Conceptual flow:

```text
Authentication
      ↓
DaoAuthenticationProvider
      ↓
UserDetailsService
      ↓
UserDetails
      ↓
Password Verification
      ↓
Success / Failure
```

---

# 14. UserDetails

Spring Security uses:

```java
UserDetails
```

to represent core user information used during authentication.

Conceptually:

```text
UserDetails
    │
    ├── Username
    ├── Password
    ├── Authorities
    └── Account Status / Flags
```

Example:

```text
Username:
vp

Password:
encoded password

Authorities:
ROLE_ADMIN
```

---

# 15. UserDetailsService

`UserDetailsService` is used to load user information by username.

Important method:

```java
UserDetails loadUserByUsername(String username)
```

Conceptually:

```text
Username
   ↓
UserDetailsService
   ↓
UserDetails
```

It is especially important for database authentication.

---

# 16. In-Memory Authentication

For learning, we created users directly in application memory.

Example:

```java
@Bean
public UserDetailsService userDetailsService() {

    UserDetails user = User
            .withUsername("vijay")
            .password("{noop}1234")
            .roles("USER")
            .build();

    UserDetails admin = User
            .withUsername("vp")
            .password("{noop}123")
            .roles("ADMIN")
            .build();

    return new InMemoryUserDetailsManager(user, admin);
}
```

This creates two users:

```text
vijay
Password: 1234
Role: ROLE_USER
```

and:

```text
vp
Password: 123
Role: ROLE_ADMIN
```

---

# 17. Why `{noop}`?

We used:

```java
.password("{noop}1234")
```

only for learning/demo purposes.

`{noop}` tells Spring Security that the password is being treated without password encoding.

Example:

```java
.password("{noop}1234")
```

Production applications should use a proper `PasswordEncoder`.

Password encoding will be covered separately in:

```text
09_PasswordEncoder.md
```

---

# 18. InMemoryUserDetailsManager

This class stores users in memory.

Example:

```java
return new InMemoryUserDetailsManager(user, admin);
```

Meaning:

```text
Application Memory
      │
      ├── vijay
      │     └── ROLE_USER
      │
      └── vp
            └── ROLE_ADMIN
```

No database is involved in this setup.

---

# 19. Form Login

We enabled form-based authentication using:

```java
.formLogin(form -> form
        .permitAll()
)
```

Our security configuration:

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
                    .requestMatchers("/hello").permitAll()
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                    .permitAll()
            );

    return http.build();
}
```

Now protected endpoints can redirect the user to the login page.

---

# 20. Protected Endpoint

Our rule:

```java
.anyRequest().authenticated()
```

means:

> Every request not explicitly permitted requires an authenticated user.

Example:

```text
/hello
   ↓
Public

/user
   ↓
Authentication Required

/admin
   ↓
Authentication Required
```

---

# 21. Multiple Users

We created:

```text
User 1:

Username: vijay
Password: 1234
Role: ROLE_USER
```

and:

```text
User 2:

Username: vp
Password: 123
Role: ROLE_ADMIN
```

The authentication system verifies the credentials and establishes the corresponding authenticated identity.

---

# 22. Authentication Practical Flow

When `vp` logs in:

```text
Username: vp
Password: 123
```

Flow:

```text
Login
 ↓
UsernamePasswordAuthenticationFilter
 ↓
Authentication
 ↓
AuthenticationManager
 ↓
AuthenticationProvider
 ↓
UserDetailsService
 ↓
InMemoryUserDetailsManager
 ↓
UserDetails
 ↓
Password Verification
 ↓
Authentication Success
 ↓
SecurityContext
 ↓
Protected Resource
```

---

# 23. Authentication Success

If credentials are valid:

```text
Username: vp
Password: 123
        ↓
AuthenticationProvider
        ↓
Password Verification
        ↓
SUCCESS
```

An authenticated `Authentication` is established.

Then:

```text
Authentication
      ↓
SecurityContext
```

The application can now identify the current authenticated user.

---

# 24. Authentication Failure

If credentials are incorrect:

```text
Username: vp
Password: wrong
        ↓
AuthenticationProvider
        ↓
Password Verification
        ↓
FAILURE
```

Authentication fails and Spring Security performs the configured authentication failure handling.

---

# 25. Accessing Current User

We created this endpoint:

```java
@GetMapping("/me")
public String me(Authentication authentication) {

    return authentication.getName()
            + " - "
            + authentication.getAuthorities();
}
```

After login as:

```text
vp
123
```

calling:

```text
/me
```

can return:

```text
vp - [ROLE_ADMIN]
```

For:

```text
vijay
1234
```

it can return:

```text
vijay - [ROLE_USER]
```

---

# 26. `authentication.getName()`

```java
authentication.getName()
```

returns the authenticated user's name/identifier.

Example:

```text
vp
```

---

# 27. `authentication.getAuthorities()`

```java
authentication.getAuthorities()
```

returns the user's authorities.

Example:

```text
[ROLE_ADMIN]
```

or:

```text
[ROLE_USER]
```

---

# 28. `authentication.getPrincipal()`

```java
authentication.getPrincipal()
```

returns the principal object representing the authenticated user.

In our username/password setup, this is commonly a `UserDetails`-based principal.

Conceptually:

```text
Authentication
      ↓
Principal
      ↓
UserDetails
      ↓
Username
Authorities
Account Information
```

Important:

> `getPrincipal()` is not simply the username string. It represents the authenticated user.

---

# 29. `authentication.getCredentials()`

```java
authentication.getCredentials()
```

represents credentials associated with authentication.

In username/password authentication, credentials can relate to the password.

Do not expose credentials in API responses or logs.

For production code:

```text
getCredentials()
    ↓
Do NOT expose sensitive information
```

---

# 30. `authentication.getDetails()`

```java
authentication.getDetails()
```

returns additional authentication/request-related details when available.

In web authentication, details can contain information related to the request, depending on the authentication mechanism.

It should not be confused with:

```text
Principal
Authorities
```

---

# 31. Authentication Object Structure

Conceptually:

```text
Authentication
│
├── Principal
│      ↓
│    UserDetails / User Identity
│
├── Credentials
│      ↓
│    Authentication Credentials
│
├── Authorities
│      ↓
│    ROLE_USER / ROLE_ADMIN
│
└── Details
       ↓
    Additional Authentication Details
```

---

# 32. SecurityContext

After successful authentication, Spring Security associates the current authentication with a:

```java
SecurityContext
```

Conceptually:

```text
SecurityContext
      ↓
Authentication
      ↓
Current Authenticated User
```

Example:

```text
SecurityContext
      ↓
Authentication
      ↓
Username: vp
Authorities: ROLE_ADMIN
```

---

# 33. SecurityContextHolder

Spring Security provides:

```java
SecurityContextHolder
```

to access the current `SecurityContext`.

Example:

```java
Authentication authentication =
        SecurityContextHolder
                .getContext()
                .getAuthentication();
```

Then:

```java
String username = authentication.getName();
```

---

# 34. Practical SecurityContext Example

Controller:

```java
@GetMapping("/context")
public String context() {

    Authentication authentication =
            SecurityContextHolder
                    .getContext()
                    .getAuthentication();

    return authentication.getName()
            + " - "
            + authentication.getAuthorities();
}
```

If logged in as:

```text
vp
123
```

Result:

```text
vp - [ROLE_ADMIN]
```

---

# 35. Authentication Parameter vs SecurityContextHolder

### Method 1

Spring can provide `Authentication` directly:

```java
@GetMapping("/me")
public String me(Authentication authentication) {

    return authentication.getName();
}
```

### Method 2

Access it through `SecurityContextHolder`:

```java
@GetMapping("/context")
public String context() {

    Authentication authentication =
            SecurityContextHolder
                    .getContext()
                    .getAuthentication();

    return authentication.getName();
}
```

Both allow access to the current authentication.

---

# 36. UsernamePasswordAuthenticationToken

`UsernamePasswordAuthenticationToken` represents username/password authentication information.

Example:

```java
new UsernamePasswordAuthenticationToken(
        username,
        password
);
```

Conceptually:

```text
Username
+
Password
      ↓
UsernamePasswordAuthenticationToken
      ↓
AuthenticationManager
```

This becomes especially important when manually authenticating a login request, such as in a JWT-based login flow.

---

# 37. AuthenticationManager Practical Configuration

We can expose the configured `AuthenticationManager` as a Spring Bean:

```java
@Bean
public AuthenticationManager authenticationManager(
        AuthenticationConfiguration configuration)
        throws Exception {

    return configuration.getAuthenticationManager();
}
```

Imports:

```java
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
```

This obtains the configured authentication manager from Spring Security.

---

# 38. AuthenticationManager and AuthenticationProvider

Relationship:

```text
AuthenticationManager
        ↓
AuthenticationProvider
        ↓
User Authentication
```

Simple meaning:

```text
AuthenticationManager
    ↓
Authentication process coordinate/delegate karta hai.

AuthenticationProvider
    ↓
Particular authentication mechanism perform karta hai.
```

---

# 39. AuthenticationManager vs UserDetailsService

These are different.

### AuthenticationManager

```text
Authentication process ko manage/delegate karta hai.
```

### UserDetailsService

```text
User details load karta hai.
```

Flow:

```text
AuthenticationManager
        ↓
AuthenticationProvider
        ↓
UserDetailsService
        ↓
UserDetails
```

Simple:

```text
UserDetailsService
    ↓
"User ka data lao."

AuthenticationProvider
    ↓
"Credentials verify karo."

AuthenticationManager
    ↓
"Authentication process manage/delegate karo."
```

---

# 40. AuthenticationProvider and PasswordEncoder

`AuthenticationProvider` performs the authentication process.

`PasswordEncoder` is responsible for password encoding and matching.

Conceptual flow:

```text
AuthenticationProvider
        ↓
PasswordEncoder
        ↓
Password Verification
```

`PasswordEncoder` will be covered in:

```text
09_PasswordEncoder.md
```

---

# 41. Database Authentication

In real applications, users are normally stored in a database.

Conceptual flow:

```text
Login
 ↓
Authentication
 ↓
AuthenticationProvider
 ↓
UserDetailsService
 ↓
Database
 ↓
UserDetails
 ↓
Password Verification
 ↓
Authentication Success
```

Database authentication will be covered in:

```text
07_Database_Authentication.md
```

---

# 42. JWT Authentication Connection

JWT authentication uses the same authentication concepts.

Login:

```text
Username + Password
       ↓
AuthenticationManager
       ↓
AuthenticationProvider
       ↓
UserDetailsService
       ↓
Password Verification
       ↓
Authentication Success
       ↓
Generate JWT
```

Later request:

```text
Request
   ↓
Authorization: Bearer JWT
   ↓
JwtAuthenticationFilter
   ↓
Validate JWT
   ↓
Identify User
   ↓
Create Authentication
   ↓
SecurityContext
   ↓
Authorization
   ↓
Controller
```

JWT will be covered in:

```text
10_JWT.md
```

---

# 43. SecurityContext in JWT

JWT filter commonly follows this concept:

```java
SecurityContextHolder
        .getContext()
        .setAuthentication(authentication);
```

Meaning:

> The current request is associated with this authenticated user.

Flow:

```text
JWT
 ↓
Validate JWT
 ↓
Create Authentication
 ↓
SecurityContextHolder
 ↓
setAuthentication()
 ↓
Controller
```

This is one of the most important concepts for JWT authentication.

---

# 44. Complete Authentication Architecture

```text
                         USER
                           │
                           │ Username + Password
                           ▼
              UsernamePasswordAuthenticationFilter
                           │
                           ▼
                  Authentication Object
                           │
                           ▼
                 AuthenticationManager
                           │
                           ▼
                AuthenticationProvider
                           │
                           ▼
                  UserDetailsService
                           │
                           ▼
                     UserDetails
                           │
                           ▼
                    PasswordEncoder
                           │
                    ┌──────┴──────┐
                    ▼             ▼
                 SUCCESS        FAILURE
                    │             │
                    ▼             ▼
             Authentication   Exception
                    │
                    ▼
              SecurityContext
                    │
                    ▼
               Authorization
                    │
                    ▼
                Controller
```

---

# 45. Authentication vs Authorization Flow

```text
HTTP Request
      ↓
Security Filter Chain
      ↓
Authentication
      ↓
   ┌──┴──┐
   ↓     ↓
Success Failure
   ↓     ↓
Security  Reject
Context
   ↓
Authorization
   ↓
 ┌─┴──┐
 ↓    ↓
Allow Deny
 ↓    ↓
Controller 403
```

Remember:

```text
Authentication
      ↓
Who are you?

Authorization
      ↓
What can you access?
```

---

# 46. Our Current Project Flow

Our project currently works approximately like this:

```text
Browser
   ↓
/login
   ↓
UsernamePasswordAuthenticationFilter
   ↓
AuthenticationManager
   ↓
AuthenticationProvider
   ↓
InMemoryUserDetailsManager
   ↓
UserDetails
   ↓
Password Verification
   ↓
Authentication Success
   ↓
SecurityContext
   ↓
/me
   ↓
Authentication
   ↓
Current User
```

---

# 47. Current Users

### User

```text
Username: vijay
Password: 1234
Role: ROLE_USER
```

### Admin

```text
Username: vp
Password: 123
Role: ROLE_ADMIN
```

These are currently stored in application memory.

---

# 48. Important Security Rule

Do not expose passwords/credentials.

Bad:

```java
return authentication.getCredentials();
```

Good:

```java
return authentication.getName()
        + " - "
        + authentication.getAuthorities();
```

For production applications:

```text
Passwords
Credentials
Tokens
Secrets
```

must be handled carefully and should not be exposed in responses or logs.

---

# 49. Interview Questions

## Q1. What is Authentication?

Authentication is the process of verifying the identity of a user.

---

## Q2. What is the difference between Authentication and Authorization?

```text
Authentication
    ↓
Who are you?

Authorization
    ↓
What are you allowed to access?
```

---

## Q3. What is AuthenticationManager?

`AuthenticationManager` is the central interface responsible for processing/delegating authentication requests.

---

## Q4. What is AuthenticationProvider?

`AuthenticationProvider` performs authentication for a particular authentication mechanism.

---

## Q5. What is DaoAuthenticationProvider?

It is a commonly used Spring Security provider for username/password authentication that works with `UserDetailsService` and password verification.

---

## Q6. What is UserDetailsService?

`UserDetailsService` loads user-specific data by username.

---

## Q7. What is UserDetails?

`UserDetails` represents core user information used by Spring Security.

---

## Q8. What is Authentication?

`Authentication` represents the authentication information of the current user.

---

## Q9. What does `getName()` do?

It returns the authenticated user's name/identifier.

---

## Q10. What does `getAuthorities()` do?

It returns the authorities assigned to the authenticated user.

---

## Q11. What does `getPrincipal()` do?

It returns the principal object representing the authenticated user.

---

## Q12. What does `getCredentials()` do?

It returns credentials associated with the authentication when available. Sensitive credentials should not be exposed.

---

## Q13. What does `getDetails()` do?

It returns additional authentication/request-related details when available.

---

## Q14. What is SecurityContext?

`SecurityContext` holds security information, including the current `Authentication` when available.

---

## Q15. What is SecurityContextHolder?

`SecurityContextHolder` provides access to the current `SecurityContext`.

---

## Q16. What is UsernamePasswordAuthenticationToken?

It is an `Authentication` implementation commonly used to represent username/password authentication information.

---

## Q17. What happens when authentication succeeds?

An authenticated `Authentication` is established and associated with the current security context.

---

## Q18. What happens when authentication fails?

Authentication fails and Spring Security performs the configured failure handling.

---

## Q19. What is In-Memory Authentication?

Authentication where user details are stored in application memory instead of a database.

---

## Q20. What is the role of PasswordEncoder?

`PasswordEncoder` provides password encoding and password matching/verification.

---

# 50. Key Takeaways

### Authentication

```text
Who are you?
```

### AuthenticationManager

```text
Central authentication interface
```

### AuthenticationProvider

```text
Performs authentication for a particular mechanism
```

### UserDetailsService

```text
Loads user details
```

### UserDetails

```text
Represents user information
```

### Authentication

```text
Represents current authentication information
```

### SecurityContext

```text
Holds current security information including Authentication
```

### SecurityContextHolder

```text
Provides access to current SecurityContext
```

### InMemoryUserDetailsManager

```text
Stores users in application memory
```

### PasswordEncoder

```text
Encodes and verifies passwords
```

---

# 51. Final Mental Model

```text
Username + Password
        ↓
Authentication Filter
        ↓
AuthenticationManager
        ↓
AuthenticationProvider
        ↓
UserDetailsService
        ↓
UserDetails
        ↓
Password Verification
        ↓
Authentication Success
        ↓
Authentication
        ↓
SecurityContext
        ↓
Authorization
        ↓
Controller
```

One-line revision:

> Authentication is the process of verifying a user's identity. Spring Security uses components such as AuthenticationManager, AuthenticationProvider, UserDetailsService, UserDetails, and SecurityContext to authenticate and maintain information about the authenticated user.