# 🔐 07 — Database Authentication

## 1. What is Database Authentication?

Database Authentication means that user information required for authentication is stored in a database instead of being hardcoded in the application.

In-Memory Authentication:

```text
Java Code
   ↓
InMemoryUserDetailsManager
   ↓
UserDetails
   ↓
Authentication
```

Database Authentication:

```text
MySQL
   ↓
users table
   ↓
UserRepository
   ↓
CustomUserDetailsService
   ↓
UserDetails
   ↓
Authentication
```

In real-world applications, users are generally stored in a database.

---

# 2. Why Database Authentication?

In-Memory users are useful for learning and testing, but real applications normally need persistent users.

Database Authentication provides:

- Persistent users
- Dynamic user management
- User registration
- Login using database credentials
- Role management
- Large-scale user management
- Integration with real applications

Example:

```text
Application
     ↓
Register User
     ↓
MySQL
     ↓
Login
     ↓
Database Authentication
```

---

# 3. Basic Architecture

Database Authentication flow:

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
          CustomUserDetailsService
                       ↓
                UserRepository
                       ↓
                    MySQL
                       ↓
                 users table
                       ↓
                  User Entity
                       ↓
                  UserDetails
                       ↓
              Password Verification
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

# 4. Database Setup

Create database:

```sql
CREATE DATABASE spring_security_db;
```

Select database:

```sql
USE spring_security_db;
```

---

# 5. Users Table

Create the users table:

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
```

Table structure:

```text
users
 ├── id
 ├── username
 ├── password
 └── role
```

Check table:

```sql
DESC users;
```

---

# 6. Example Database Data

For the current learning setup:

```sql
INSERT INTO users (username, password, role)
VALUES
('vijay', '{noop}1234', 'USER'),
('vp', '{noop}123', 'ADMIN');
```

Check:

```sql
SELECT * FROM users;
```

Expected:

```text
id | username | password     | role
---|----------|--------------|------
1  | vijay    | {noop}1234   | USER
2  | vp       | {noop}123     | ADMIN
```

Important:

`{noop}` is being used only for learning/testing.

Production applications should use a proper `PasswordEncoder`.

---

# 7. Required Dependencies

For database authentication, we need:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

These provide:

```text
Spring Security
JPA / Hibernate
MySQL Driver
```

---

# 8. Database Configuration

`application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/spring_security_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Replace:

```text
YOUR_PASSWORD
```

with the actual MySQL password.

---

# 9. Project Structure

Recommended structure:

```text
src/main/java/com/vijay/spring_security
│
├── Config
│   ├── SecurityConfig.java
│   └── MyFilter.java
│
├── Controller
│   └── TestController.java
│
├── Entity
│   └── User.java
│
├── Repository
│   └── UserRepository.java
│
├── Service
│   └── CustomUserDetailsService.java
│
└── SpringSecurityApplication.java
```

---

# 10. User Entity

Create:

```text
Entity/User.java
```

Code:

```java
package com.vijay.spring_security.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    public User() {
    }

    public User(Long id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
```

---

# 11. Role of User Entity

The entity maps the Java object to the database table.

Conceptually:

```text
Java User Object
       ↕
users table
```

Example:

```text
Database Row
     ↓
User Entity Object
```

The entity itself does not perform authentication.

Its main job is database mapping.

---

# 12. UserRepository

Create:

```text
Repository/UserRepository.java
```

Code:

```java
package com.vijay.spring_security.Repository;

import com.vijay.spring_security.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
```

---

# 13. Role of UserRepository

`UserRepository` is responsible for accessing the database through Spring Data JPA.

Important method:

```java
Optional<User> findByUsername(String username);
```

Conceptually, Spring Data JPA generates:

```sql
SELECT *
FROM users
WHERE username = ?;
```

So:

```text
UserRepository
      ↓
Database Access
```

---

# 14. Why Optional?

The username may or may not exist in the database.

Therefore:

```java
Optional<User>
```

represents:

```text
User found
    OR
User not found
```

Example:

```java
Optional<User> user =
        userRepository.findByUsername(username);
```

---

# 15. CustomUserDetailsService

Create:

```text
Service/CustomUserDetailsService.java
```

Code:

```java
package com.vijay.spring_security.Service;

import com.vijay.spring_security.Entity.User;
import com.vijay.spring_security.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with username: "
                                        + username
                        )
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}
```

---

# 16. Why `implements UserDetailsService`?

`UserDetailsService` is an interface.

Therefore:

```java
public class CustomUserDetailsService
        implements UserDetailsService
```

is correct.

Not:

```java
extends UserDetailsService
```

General Java rule:

```text
Class extends Class
Class implements Interface
```

---

# 17. Role of CustomUserDetailsService

Its main job is:

> Load the user from the database and convert the database user into a Spring Security `UserDetails`.

Flow:

```text
Spring Security
       ↓
CustomUserDetailsService
       ↓
UserRepository
       ↓
MySQL
       ↓
User
       ↓
UserDetails
```

---

# 18. `loadUserByUsername()`

Important method:

```java
loadUserByUsername(String username)
```

Spring Security provides the username.

Example:

```text
vijay
```

Then:

```java
userRepository.findByUsername(username);
```

searches the database.

Conceptually:

```sql
SELECT *
FROM users
WHERE username = 'vijay';
```

---

# 19. User Found

Suppose database contains:

```text
vijay | {noop}1234 | USER
```

Repository returns:

```java
User
```

Then:

```java
user.getUsername()
```

returns:

```text
vijay
```

```java
user.getPassword()
```

returns:

```text
{noop}1234
```

```java
user.getRole()
```

returns:

```text
USER
```

---

# 20. User Not Found

If username doesn't exist:

```text
Username:
rahul
```

and database has no `rahul`.

Then:

```java
.orElseThrow(() ->
        new UsernameNotFoundException(...)
)
```

throws:

```text
UsernameNotFoundException
```

Flow:

```text
rahul
 ↓
UserRepository
 ↓
User Not Found
 ↓
UsernameNotFoundException
 ↓
Authentication Failure
```

---

# 21. Database User to UserDetails

Our database entity:

```text
com.vijay.spring_security.Entity.User
```

is our application's User class.

Spring Security requires:

```text
UserDetails
```

Therefore we convert:

```java
return org.springframework.security.core.userdetails.User
        .withUsername(user.getUsername())
        .password(user.getPassword())
        .roles(user.getRole())
        .build();
```

Flow:

```text
Database User
      ↓
User Entity
      ↓
Spring Security UserDetails
```

---

# 22. Why Fully Qualified User?

Our application has:

```java
com.vijay.spring_security.Entity.User
```

Spring Security also has:

```java
org.springframework.security.core.userdetails.User
```

Both classes have the name:

```text
User
```

Therefore we use:

```java
org.springframework.security.core.userdetails.User
```

to clearly specify the Spring Security class.

---

# 23. Security Configuration

After moving to database authentication, remove the In-Memory configuration:

```java
@Bean
public UserDetailsService userDetailsService() {

    UserDetails user = ...
    UserDetails admin = ...

    return new InMemoryUserDetailsManager(user, admin);
}
```

We no longer need:

```text
InMemoryUserDetailsManager
```

because users now come from MySQL.

---

# 24. SecurityConfig

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
}
```

If you are using your custom `MyFilter`, it can remain in the filter chain as configured earlier.

---

# 25. Complete Authentication Flow

Suppose:

```text
Username: vijay
Password: 1234
```

Flow:

```text
Browser
   ↓
Login
   ↓
UsernamePasswordAuthenticationFilter
   ↓
AuthenticationManager
   ↓
AuthenticationProvider
   ↓
CustomUserDetailsService
   ↓
UserRepository
   ↓
MySQL
   ↓
users table
   ↓
User Entity
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

# 26. AuthenticationProvider

The `AuthenticationProvider` is responsible for handling authentication for a particular authentication mechanism.

For username/password authentication, Spring Security can use a provider such as:

```text
DaoAuthenticationProvider
```

Conceptually:

```text
AuthenticationManager
        ↓
DaoAuthenticationProvider
        ↓
UserDetailsService
        ↓
Database
```

---

# 27. AuthenticationManager

`AuthenticationManager` is the central authentication interface.

Conceptually:

```text
Login Credentials
       ↓
AuthenticationManager
       ↓
AuthenticationProvider
```

It coordinates/delegates authentication to an appropriate provider.

---

# 28. DaoAuthenticationProvider

For database-backed username/password authentication, a common provider is:

```text
DaoAuthenticationProvider
```

Conceptually:

```text
DaoAuthenticationProvider
          ↓
UserDetailsService
          ↓
UserRepository
          ↓
Database
```

It obtains user details and performs authentication using the configured password handling.

---

# 29. Password Verification

Currently we are using:

```text
{noop}
```

for learning.

Example:

```text
Database:
{noop}1234
```

Login:

```text
1234
```

Password verification succeeds in this learning configuration.

For production:

```text
Raw Password
     ↓
PasswordEncoder
     ↓
Encoded Password
     ↓
Database
```

and during login:

```text
Raw Password
     ↓
PasswordEncoder.matches()
     ↓
Stored Encoded Password
```

PasswordEncoder will be covered in:

```text
09_PasswordEncoder.md
```

---

# 30. Wrong Password Flow

Suppose:

```text
Username: vijay
Password: wrong123
```

Database:

```text
Username: vijay
Password: {noop}1234
```

User is found:

```text
vijay → Found ✅
```

But credentials don't match:

```text
wrong123 ≠ 1234
```

Therefore:

```text
Authentication Failed ❌
```

Important:

> Finding a user in the database does not automatically mean authentication succeeded.

---

# 31. Wrong Username Flow

Suppose:

```text
Username: rahul
Password: 1234
```

Database:

```text
rahul → Not Found
```

Then:

```text
UserRepository
      ↓
Optional.empty()
      ↓
UsernameNotFoundException
      ↓
Authentication Failure
```

---

# 32. Database Authentication with Authorization

Database authentication can work together with roles.

Database:

```text
vijay | {noop}1234 | USER
vp    | {noop}123  | ADMIN
```

Security rules:

```java
.requestMatchers("/admin")
.hasRole("ADMIN")

.requestMatchers("/user")
.hasAnyRole("USER", "ADMIN")
```

Result:

```text
vijay
ROLE_USER
   ↓
/user  → ✅
/admin → ❌
```

Admin:

```text
vp
ROLE_ADMIN
   ↓
/user  → ✅
/admin → ✅
```

---

# 33. Database Authentication vs In-Memory Authentication

## In-Memory

```text
Java Code
   ↓
InMemoryUserDetailsManager
   ↓
UserDetails
```

Advantages:

- Easy setup
- No database
- Good for learning
- Good for testing

Disadvantages:

- Not persistent
- Users are defined in application configuration
- Not suitable for most production applications

---

## Database Authentication

```text
MySQL
   ↓
UserRepository
   ↓
CustomUserDetailsService
   ↓
UserDetails
```

Advantages:

- Persistent users
- Dynamic user management
- Suitable for real applications
- Supports registration systems
- Easy to manage many users

---

# 34. Application Restart

In-Memory:

```text
Application Stop
      ↓
Memory Cleared
      ↓
Users Lost
```

Database:

```text
Application Stop
      ↓
Database remains
      ↓
Users remain
```

Therefore database authentication provides persistence.

---

# 35. Real Registration Flow

In a real application:

```text
POST /register
       ↓
Receive User Data
       ↓
PasswordEncoder
       ↓
Create User Entity
       ↓
UserRepository.save()
       ↓
MySQL
```

Then login:

```text
POST /login
       ↓
Username + Password
       ↓
AuthenticationManager
       ↓
AuthenticationProvider
       ↓
CustomUserDetailsService
       ↓
MySQL
       ↓
UserDetails
       ↓
Password Verification
       ↓
Authentication Success
```

---

# 36. Database Authentication with JWT

Later, JWT authentication can use the same database authentication process.

Login:

```text
Username + Password
       ↓
AuthenticationManager
       ↓
AuthenticationProvider
       ↓
CustomUserDetailsService
       ↓
MySQL
       ↓
Password Verification
       ↓
Authentication Success
       ↓
Generate JWT
       ↓
Return JWT
```

Later requests:

```text
Request
   ↓
Authorization: Bearer JWT
   ↓
JWT Filter
   ↓
Validate JWT
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

# 37. Complete Architecture

```text
                         LOGIN
                           ↓
              Username + Password
                           ↓
        UsernamePasswordAuthenticationFilter
                           ↓
                  AuthenticationManager
                           ↓
                 AuthenticationProvider
                           ↓
                DaoAuthenticationProvider
                           ↓
                UserDetailsService
                           ↓
             CustomUserDetailsService
                           ↓
                   UserRepository
                           ↓
                       MySQL
                           ↓
                    users table
                           ↓
                      User Entity
                           ↓
                     UserDetails
                           ↓
                 Password Verification
                           ↓
                  Authentication Success
                           ↓
                   SecurityContext
                           ↓
                    Authorization
                           ↓
                      Controller
```

---

# 38. Important Components

| Component | Main Responsibility |
|---|---|
| `User` Entity | Maps Java object to database |
| `UserRepository` | Accesses users from database |
| `UserDetailsService` | Loads user details |
| `CustomUserDetailsService` | Custom database-based implementation |
| `UserDetails` | Represents security user information |
| `AuthenticationProvider` | Performs/delegates authentication for a mechanism |
| `DaoAuthenticationProvider` | Common username/password provider using `UserDetailsService` |
| `AuthenticationManager` | Central authentication interface |
| `SecurityContext` | Holds current authentication information |

---

# 39. Important Code

## Entity

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String role;
}
```

## Repository

```java
public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
```

## Service

```java
@Service
@AllArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with username: "
                                        + username
                        )
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}
```

---

# 40. Important Mistake

Wrong:

```java
public class CustomUserDetailsService
        extends UserDetailsService
```

Correct:

```java
public class CustomUserDetailsService
        implements UserDetailsService
```

Because:

```text
UserDetailsService = Interface
```

Java rule:

```text
Class extends Class
Class implements Interface
```

---

# 41. Important `roles()` Concept

Database contains:

```text
USER
```

Code:

```java
.roles(user.getRole())
```

Spring Security generally treats it as:

```text
ROLE_USER
```

Database:

```text
ADMIN
```

becomes:

```text
ROLE_ADMIN
```

Therefore:

```java
.hasRole("ADMIN")
```

matches:

```text
ROLE_ADMIN
```

---

# 42. Important Security Rule

Never store real production passwords using:

```text
{noop}
```

or plain text.

Current:

```text
{noop}1234
```

is only for learning.

Production:

```text
1234
  ↓
PasswordEncoder
  ↓
Encoded password
  ↓
Database
```

Use a strong password hashing strategy such as BCrypt through Spring Security's `PasswordEncoder`.

---

# 43. Interview Questions

## Q1. What is Database Authentication?

Database Authentication is a process where user details are loaded from a database and used by Spring Security to authenticate the user.

---

## Q2. Why use Database Authentication?

Because users need to be persistent and dynamically managed in real-world applications.

---

## Q3. What is the role of UserRepository?

It provides database access for user data.

---

## Q4. What is the role of UserDetailsService?

It loads user details by username for Spring Security.

---

## Q5. Why create CustomUserDetailsService?

Because Spring Security needs a way to load our application's database users as `UserDetails`.

---

## Q6. What does `loadUserByUsername()` do?

It loads a user by username and returns a `UserDetails` object.

---

## Q7. What happens if the user does not exist?

A `UsernameNotFoundException` is thrown.

---

## Q8. Why convert our User entity to UserDetails?

Because our application entity represents database data, while `UserDetails` represents the security information Spring Security uses for authentication.

---

## Q9. What is AuthenticationManager?

It is the central interface responsible for processing/delegating authentication requests.

---

## Q10. What is AuthenticationProvider?

It handles authentication for a particular authentication mechanism.

---

## Q11. What is DaoAuthenticationProvider?

It is a common Spring Security provider for username/password authentication that uses `UserDetailsService` and password verification.

---

## Q12. Is In-Memory Authentication persistent?

No. In-memory users are stored in application memory and are recreated when the application starts.

---

## Q13. Is Database Authentication persistent?

Yes. User records remain in the database even when the application restarts.

---

## Q14. Why shouldn't `{noop}` be used in production?

Because it does not provide secure password hashing. Production applications should use a proper `PasswordEncoder`.

---

# 44. Key Takeaways

```text
Database Authentication
        ↓
Users stored in MySQL
```

```text
UserRepository
        ↓
Fetch user from database
```

```text
CustomUserDetailsService
        ↓
Convert database user → UserDetails
```

```text
AuthenticationProvider
        ↓
Authenticate user
```

```text
AuthenticationManager
        ↓
Coordinate/delegate authentication
```

```text
SecurityContext
        ↓
Store current authentication information
```

---

# 45. Final Mental Model

```text
                   USER LOGIN
                       ↓
                Username + Password
                       ↓
          UsernamePasswordAuthenticationFilter
                       ↓
              AuthenticationManager
                       ↓
             AuthenticationProvider
                       ↓
          CustomUserDetailsService
                       ↓
                UserRepository
                       ↓
                    MySQL
                       ↓
                  users table
                       ↓
                  User Entity
                       ↓
                 UserDetails
                       ↓
              Password Verification
                       ↓
              Authentication Success
                       ↓
                SecurityContext
                       ↓
                 Authorization
                       ↓
                  Controller
```

---

# 46. One-Line Revision

> Database Authentication allows Spring Security to authenticate users whose security information is stored persistently in a database by loading the user through `UserDetailsService` and verifying the provided credentials.