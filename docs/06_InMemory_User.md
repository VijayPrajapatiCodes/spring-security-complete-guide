# 🔐 06 — In-Memory User

## 1. What is In-Memory Authentication?

In-Memory Authentication means storing user details inside the application's memory instead of storing them in a database.

Instead of:

```text
MySQL
  ↓
users table
  ↓
username / password / role
```

we use:

```text
Spring Boot Application
        ↓
InMemoryUserDetailsManager
        ↓
UserDetails
```

It is mainly useful for:

- Learning
- Testing
- Development
- Small demo applications
- Prototypes

For production applications, users are generally stored in a database.

---

# 2. Why Use In-Memory Users?

In-Memory users are useful when we don't want to configure a database yet.

For example:

```text
Spring Security Learning
        ↓
No Database Required
        ↓
Create Users Directly in Java
        ↓
Test Authentication
```

This allows us to understand Spring Security before implementing database authentication.

---

# 3. UserDetails

Spring Security provides:

```java
UserDetails
```

to represent the user's security information.

Example:

```java
UserDetails user = User
        .withUsername("vijay")
        .password("{noop}1234")
        .roles("USER")
        .build();
```

This creates a user with:

```text
Username: vijay
Password: 1234
Role: ROLE_USER
```

---

# 4. Creating a User

Basic syntax:

```java
UserDetails user = User
        .withUsername("vijay")
        .password("{noop}1234")
        .roles("USER")
        .build();
```

Let's understand each part.

### `withUsername()`

```java
.withUsername("vijay")
```

Defines the username.

```text
Username = vijay
```

---

### `password()`

```java
.password("{noop}1234")
```

Defines the user's password.

Here:

```text
{noop}
```

is being used for learning/testing so Spring Security treats the password as having no encoding.

Production applications should use a proper `PasswordEncoder`.

---

### `roles()`

```java
.roles("USER")
```

Assigns a role to the user.

Spring Security generally represents it as:

```text
ROLE_USER
```

Similarly:

```java
.roles("ADMIN")
```

becomes:

```text
ROLE_ADMIN
```

---

### `build()`

```java
.build();
```

Creates the `UserDetails` object.

---

# 5. Creating an Admin User

Example:

```java
UserDetails admin = User
        .withUsername("vp")
        .password("{noop}123")
        .roles("ADMIN")
        .build();
```

Result:

```text
Username: vp
Password: 123
Role: ROLE_ADMIN
```

---

# 6. InMemoryUserDetailsManager

Spring Security provides:

```java
InMemoryUserDetailsManager
```

to manage users stored in memory.

Example:

```java
return new InMemoryUserDetailsManager(user, admin);
```

This means both users are registered with the manager.

Conceptually:

```text
InMemoryUserDetailsManager
        │
        ├── vijay
        │     └── ROLE_USER
        │
        └── vp
              └── ROLE_ADMIN
```

---

# 7. Complete UserDetailsService Configuration

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

---

# 8. Why Return UserDetailsService?

`InMemoryUserDetailsManager` implements:

```java
UserDetailsService
```

Therefore we can return it from:

```java
@Bean
public UserDetailsService userDetailsService()
```

Conceptually:

```text
UserDetailsService
       ↑
       │ implements
       │
InMemoryUserDetailsManager
```

---

# 9. Multiple Users

We can create multiple users.

Example:

```java
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

UserDetails manager = User
        .withUsername("manager")
        .password("{noop}manager123")
        .roles("MANAGER")
        .build();

return new InMemoryUserDetailsManager(
        user,
        admin,
        manager
);
```

Now:

```text
vijay
   ↓
ROLE_USER

vp
   ↓
ROLE_ADMIN

manager
   ↓
ROLE_MANAGER
```

---

# 10. Important Mistake

Suppose we create:

```java
UserDetails admin = User
        .withUsername("vp")
        .password("{noop}123")
        .roles("ADMIN")
        .build();
```

But return:

```java
return new InMemoryUserDetailsManager(user);
```

Then `admin` is NOT registered with the manager.

So:

```text
admin object created       ✅
admin registered           ❌
```

Therefore login with `vp` will fail.

Correct:

```java
return new InMemoryUserDetailsManager(user, admin);
```

---

# 11. In-Memory Authentication Flow

Suppose user enters:

```text
Username: vijay
Password: 1234
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
```

---

# 12. Authentication with In-Memory User

Example:

```text
Username: vijay
Password: 1234
```

Spring Security finds:

```text
UserDetails
    ↓
username = vijay
password = 1234
authorities = ROLE_USER
```

Then authentication succeeds if credentials are valid.

---

# 13. Authorization with In-Memory Users

In-Memory users can also be used with authorization.

Example:

```java
.requestMatchers("/admin")
.hasRole("ADMIN")

.requestMatchers("/user")
.hasAnyRole("USER", "ADMIN")
```

Now:

```text
vijay
ROLE_USER
   ↓
/user  → ✅
/admin → ❌
```

And:

```text
vp
ROLE_ADMIN
   ↓
/user  → ✅
/admin → ✅
```

---

# 14. Role Assignment

Example:

```java
.roles("USER")
```

means:

```text
ROLE_USER
```

Example:

```java
.roles("ADMIN")
```

means:

```text
ROLE_ADMIN
```

Example:

```java
.roles("MANAGER")
```

means:

```text
ROLE_MANAGER
```

---

# 15. Role-Based Authorization Example

```java
.authorizeHttpRequests(auth -> auth

        .requestMatchers("/hello")
        .permitAll()

        .requestMatchers("/user")
        .hasAnyRole("USER", "ADMIN")

        .requestMatchers("/admin")
        .hasRole("ADMIN")

        .anyRequest()
        .authenticated()
)
```

---

# 16. `hasRole()`

Example:

```java
.hasRole("ADMIN")
```

requires:

```text
ROLE_ADMIN
```

So:

```text
vp → ROLE_ADMIN → ✅
vijay → ROLE_USER → ❌
```

---

# 17. `hasAnyRole()`

Example:

```java
.hasAnyRole("USER", "ADMIN")
```

allows:

```text
ROLE_USER
     OR
ROLE_ADMIN
```

Therefore:

```text
vijay → ROLE_USER  → ✅
vp    → ROLE_ADMIN → ✅
```

---

# 18. `{noop}` Password

Example:

```java
.password("{noop}1234")
```

`{noop}` indicates that the password is not encoded.

This is acceptable for learning/demo purposes.

It should NOT be used for production password storage.

Production applications should use:

```text
PasswordEncoder
```

Example:

```text
BCryptPasswordEncoder
```

Password encoding is covered separately in:

```text
09_PasswordEncoder.md
```

---

# 19. In-Memory vs Database Authentication

## In-Memory

```text
Application Memory
       ↓
UserDetails
       ↓
Authentication
```

Advantages:

- Easy to configure
- No database required
- Good for learning
- Good for testing

Disadvantages:

- Data is not persistent
- Users are defined in application configuration/code
- Not suitable for most production applications

---

## Database Authentication

```text
MySQL
  ↓
users table
  ↓
UserDetailsService
  ↓
Authentication
```

Advantages:

- Persistent users
- Dynamic user management
- Suitable for real applications

Database authentication will be covered in:

```text
07_Database_Authentication.md
```

---

# 20. In-Memory User and Application Restart

In-memory users are recreated when the application starts.

Conceptually:

```text
Application Start
       ↓
Create Users
       ↓
Store in Memory
```

When application stops:

```text
Application Stop
       ↓
Memory Cleared
```

When application starts again:

```text
Application Start
       ↓
Users Created Again
```

Therefore, in-memory users are not persistent like database users.

---

# 21. Complete Security Configuration

Example:

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
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
}
```

---

# 22. Testing In-Memory Users

## Test User

Login:

```text
Username: vijay
Password: 1234
```

Role:

```text
ROLE_USER
```

Test:

```text
/user → ✅
/admin → ❌ 403
```

---

## Test Admin

Login:

```text
Username: vp
Password: 123
```

Role:

```text
ROLE_ADMIN
```

Test:

```text
/user → ✅
/admin → ✅
```

---

# 23. In-Memory User Architecture

```text
                 User
                  ↓
          UserDetails Object
                  ↓
    InMemoryUserDetailsManager
                  ↓
          UserDetailsService
                  ↓
       AuthenticationProvider
                  ↓
       AuthenticationManager
                  ↓
          Authentication
                  ↓
          SecurityContext
                  ↓
           Authorization
                  ↓
              Resource
```

---

# 24. Important Classes

### `User`

```java
org.springframework.security.core.userdetails.User
```

Used to create a `UserDetails` object.

---

### `UserDetails`

```java
org.springframework.security.core.userdetails.UserDetails
```

Represents user information required by Spring Security.

---

### `UserDetailsService`

```java
org.springframework.security.core.userdetails.UserDetailsService
```

Loads user details.

---

### `InMemoryUserDetailsManager`

```java
org.springframework.security.provisioning.InMemoryUserDetailsManager
```

Manages users stored in memory and implements `UserDetailsService`.

---

# 25. Important Methods

```java
.withUsername()
```

Defines username.

```java
.password()
```

Defines password.

```java
.roles()
```

Assigns roles.

```java
.build()
```

Creates the `UserDetails`.

```java
new InMemoryUserDetailsManager(...)
```

Registers/manages the users in memory.

---

# 26. Advantages

In-Memory Authentication provides:

- Simple configuration
- Fast setup
- No database dependency
- Easy testing
- Good for learning Spring Security
- Useful for prototypes

---

# 27. Disadvantages

In-Memory Authentication has limitations:

- Users are not persistent
- User data is recreated on application startup
- Not suitable for dynamic user registration
- Not suitable for most production applications
- Hard to manage a large number of users
- User data is generally defined by application configuration/code

---

# 28. When Should We Use It?

Good use cases:

```text
Learning
Testing
Development
Demo Applications
Small Prototypes
```

Not generally suitable for:

```text
Large Production Applications
Dynamic User Registration
Applications with Many Users
```

For those cases, use database-backed authentication.

---

# 29. Interview Questions

## Q1. What is In-Memory Authentication?

It is an authentication mechanism where user details are stored in application memory instead of a database.

---

## Q2. What is `InMemoryUserDetailsManager`?

It is a Spring Security implementation of `UserDetailsService` that manages user details in memory.

---

## Q3. What is `UserDetails`?

`UserDetails` represents the core user information used by Spring Security during authentication.

---

## Q4. Why do we use `UserDetailsService`?

It provides a mechanism for loading user details by username.

---

## Q5. What does `.roles("ADMIN")` do?

It assigns the `ADMIN` role, which Spring Security generally represents as:

```text
ROLE_ADMIN
```

---

## Q6. What is `{noop}`?

`{noop}` indicates that the password is being treated without password encoding. It is suitable only for simple learning/testing scenarios, not production password storage.

---

## Q7. Is In-Memory Authentication suitable for production?

Generally no.

Real applications usually need persistent, database-backed users and secure password handling.

---

## Q8. What happens to In-Memory users when the application restarts?

They are recreated from the application configuration because they are stored in memory and are not persistent database records.

---

## Q9. Can we create multiple In-Memory users?

Yes.

Example:

```java
return new InMemoryUserDetailsManager(
        user,
        admin,
        manager
);
```

---

## Q10. What happens if we create an admin object but don't pass it to `InMemoryUserDetailsManager`?

The user will not be registered with the manager.

Incorrect:

```java
UserDetails admin = ...;

return new InMemoryUserDetailsManager(user);
```

Correct:

```java
return new InMemoryUserDetailsManager(user, admin);
```

---

# 30. Key Takeaways

```text
In-Memory Authentication
        ↓
Users stored in application memory
```

```text
UserDetails
        ↓
Represents user security information
```

```text
InMemoryUserDetailsManager
        ↓
Manages in-memory users
```

```text
.roles("USER")
        ↓
ROLE_USER
```

```text
.roles("ADMIN")
        ↓
ROLE_ADMIN
```

```text
{noop}
        ↓
No password encoding
        ↓
Learning/demo only
```

```text
Application Restart
        ↓
In-Memory users recreated
```

---

# 31. Final Mental Model

```text
              In-Memory User
                    ↓
              UserDetails
                    ↓
      InMemoryUserDetailsManager
                    ↓
           UserDetailsService
                    ↓
        AuthenticationProvider
                    ↓
         AuthenticationManager
                    ↓
            Authentication
                    ↓
            SecurityContext
                    ↓
            Authorization
                    ↓
              Controller
```

---

# 32. One-Line Revision

> In-Memory Authentication stores Spring Security user details in application memory using `InMemoryUserDetailsManager` and is mainly useful for learning, testing, development, and simple applications.