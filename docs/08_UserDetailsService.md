# 🔐 08 — UserDetailsService

## 1. What is UserDetailsService?

`UserDetailsService` is an interface provided by Spring Security.

Its main responsibility is:

> Load user details by username.

The main method is:

```java
UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException;
```

Basic flow:

```text
Username
   ↓
UserDetailsService
   ↓
UserDetails
```

For database authentication:

```text
Username
   ↓
CustomUserDetailsService
   ↓
UserRepository
   ↓
MySQL
   ↓
User Entity
   ↓
UserDetails
   ↓
Spring Security
```

---

# 2. UserDetailsService Does NOT Perform Complete Authentication

This is very important.

`UserDetailsService` mainly loads the user.

It does not by itself:

```text
❌ Perform complete authentication
❌ Perform authorization
❌ Create JWT
❌ Create login session
```

Its main responsibility is:

```text
Load User
   ↓
Return UserDetails
```

Conceptually:

```text
UserDetailsService
       ↓
"User ka data lao"

AuthenticationProvider
       ↓
"Credentials verify karo"
```

---

# 3. UserDetailsService is an Interface

Spring Security provides:

```java
public interface UserDetailsService {

    UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException;
}
```

Because it is an interface, our class uses:

```java
implements UserDetailsService
```

Correct:

```java
public class CustomUserDetailsService
        implements UserDetailsService {
}
```

Incorrect:

```java
public class CustomUserDetailsService
        extends UserDetailsService {
}
```

Java rule:

```text
Class extends Class
Class implements Interface
```

---

# 4. Why Does Spring Security Use an Interface?

Spring Security should not care where users are stored.

Users can be stored in:

```text
MySQL
PostgreSQL
MongoDB
LDAP
In-Memory
External Service
```

Spring Security only expects:

```java
UserDetailsService
```

Therefore:

```text
                  UserDetailsService
                         ↑
             ┌───────────┼───────────┐
             │           │           │
           MySQL      MongoDB     In-Memory
```

This makes Spring Security flexible.

---

# 5. `loadUserByUsername()`

The main method:

```java
@Override
public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {
```

Spring Security provides the username.

Example:

```text
Username = vijay
```

Then Spring Security calls conceptually:

```java
loadUserByUsername("vijay");
```

---

# 6. Database Authentication Flow

Our application:

```text
Login
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
loadUserByUsername()
 ↓
UserRepository
 ↓
MySQL
 ↓
User
 ↓
UserDetails
```

After the user is loaded, authentication continues with credential verification.

---

# 7. CustomUserDetailsService

Our implementation:

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

        return new CustomUserDetails(user);
    }
}
```

---

# 8. Role of CustomUserDetailsService

Its responsibility is:

```text
Username
   ↓
Search Database
   ↓
Get User Entity
   ↓
Convert User → UserDetails
   ↓
Return UserDetails
```

So:

```text
CustomUserDetailsService
          ↓
UserRepository
          ↓
MySQL
```

---

# 9. UserRepository

Repository:

```java
public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
```

When:

```java
userRepository.findByUsername(username);
```

is called, Spring Data JPA conceptually executes:

```sql
SELECT *
FROM users
WHERE username = ?;
```

---

# 10. Why `Optional<User>`?

A user may exist or may not exist.

Therefore:

```java
Optional<User>
```

represents:

```text
User Found
     OR
User Not Found
```

Example:

```java
User user = userRepository.findByUsername(username)
        .orElseThrow(...);
```

---

# 11. UsernameNotFoundException

If the username does not exist:

```java
throw new UsernameNotFoundException(...)
```

Example:

```java
.orElseThrow(() ->
        new UsernameNotFoundException(
                "User not found with username: " + username
        )
);
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

# 12. What is UserDetails?

`UserDetails` is another Spring Security interface.

It represents the security-related information of a user.

Conceptually:

```text
UserDetails
    │
    ├── Username
    ├── Password
    ├── Authorities
    ├── Account Status
    └── Credentials Status
```

---

# 13. Important UserDetails Methods

`UserDetails` provides methods such as:

```java
String getUsername();

String getPassword();

Collection<? extends GrantedAuthority> getAuthorities();

boolean isAccountNonExpired();

boolean isAccountNonLocked();

boolean isCredentialsNonExpired();

boolean isEnabled();
```

---

# 14. `getUsername()`

Returns the username.

Example:

```java
@Override
public String getUsername() {
    return user.getUsername();
}
```

If database contains:

```text
username = vijay
```

then:

```text
getUsername()
     ↓
vijay
```

---

# 15. `getPassword()`

Returns the stored password representation.

Example:

```java
@Override
public String getPassword() {
    return user.getPassword();
}
```

Current learning example:

```text
{noop}1234
```

Production applications should use a secure `PasswordEncoder`.

---

# 16. `getAuthorities()`

Returns the authorities assigned to the user.

Example:

```text
ROLE_USER
ROLE_ADMIN
```

This is important for authorization.

Flow:

```text
UserDetails
     ↓
getAuthorities()
     ↓
ROLE_ADMIN
     ↓
Authorization
     ↓
/admin → Allowed
```

---

# 17. Account Status Methods

### `isAccountNonExpired()`

```java
return true;
```

means account is not expired.

---

### `isAccountNonLocked()`

```java
return true;
```

means account is not locked.

---

### `isCredentialsNonExpired()`

```java
return true;
```

means credentials are not expired.

---

### `isEnabled()`

```java
return true;
```

means account is enabled.

---

# 18. CustomUserDetails

Instead of using Spring Security's built-in `User`, we can create our own implementation of `UserDetails`.

Create:

```text
CustomUserDetails.java
```

Code:

```java
package com.vijay.spring_security.Service;

import com.vijay.spring_security.Entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole()
                )
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

---

# 19. Why Create CustomUserDetails?

Our application has its own:

```text
User Entity
```

Spring Security needs:

```text
UserDetails
```

Therefore:

```text
Database User Entity
        ↓
CustomUserDetails
        ↓
UserDetails
        ↓
Spring Security
```

This allows us to control how our database user is represented to Spring Security.

---

# 20. User Entity vs UserDetails

These are different concepts.

### Application User Entity

```text
com.vijay.spring_security.Entity.User
```

Purpose:

```text
Database Mapping
```

### Spring Security UserDetails

```text
org.springframework.security.core.userdetails.UserDetails
```

Purpose:

```text
Security Information
```

Flow:

```text
Database
   ↓
User Entity
   ↓
CustomUserDetails
   ↓
UserDetails
   ↓
Spring Security
```

---

# 21. CustomUserDetailsService with CustomUserDetails

Instead of:

```java
return org.springframework.security.core.userdetails.User
        .withUsername(user.getUsername())
        .password(user.getPassword())
        .roles(user.getRole())
        .build();
```

we can use:

```java
return new CustomUserDetails(user);
```

Complete:

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

        return new CustomUserDetails(user);
    }
}
```

---

# 22. GrantedAuthority

`GrantedAuthority` represents an authority granted to a user.

Examples:

```text
ROLE_USER
ROLE_ADMIN
USER_READ
USER_WRITE
USER_DELETE
```

A common implementation is:

```java
SimpleGrantedAuthority
```

Example:

```java
new SimpleGrantedAuthority("ROLE_ADMIN");
```

---

# 23. Role to Authority Conversion

Database:

```text
role = USER
```

Code:

```java
"ROLE_" + user.getRole()
```

Result:

```text
ROLE_USER
```

Database:

```text
role = ADMIN
```

Result:

```text
ROLE_ADMIN
```

Flow:

```text
ADMIN
 ↓
ROLE_ + ADMIN
 ↓
ROLE_ADMIN
 ↓
GrantedAuthority
```

---

# 24. `hasRole()` Connection

Security configuration:

```java
.requestMatchers("/admin")
.hasRole("ADMIN")
```

User authority:

```text
ROLE_ADMIN
```

Result:

```text
ROLE_ADMIN
      ↓
hasRole("ADMIN")
      ↓
Match ✅
```

For normal user:

```text
ROLE_USER
      ↓
hasRole("ADMIN")
      ↓
No Match ❌
```

---

# 25. `hasAuthority()` Connection

If authority is:

```text
USER_READ
```

then:

```java
.hasAuthority("USER_READ")
```

matches it.

Example:

```text
UserDetails
    ↓
USER_READ
    ↓
hasAuthority("USER_READ")
    ↓
Allowed
```

---

# 26. Complete UserDetails Flow

```text
Database User
      ↓
User Entity
      ↓
CustomUserDetailsService
      ↓
CustomUserDetails
      ↓
UserDetails
      ↓
┌───────────────────────────┐
│ Username                  │
│ Password                  │
│ Authorities               │
│ Account Status            │
│ Credential Status         │
└───────────────────────────┘
      ↓
AuthenticationProvider
      ↓
Authentication
      ↓
SecurityContext
      ↓
Authorization
```

---

# 27. UserDetailsService vs UserDetails

This is one of the most important interview differences.

### UserDetailsService

```text
Responsible for:
Loading the user
```

Main method:

```java
loadUserByUsername()
```

---

### UserDetails

```text
Responsible for:
Representing the loaded user's security information
```

Important methods:

```java
getUsername()
getPassword()
getAuthorities()
isEnabled()
...
```

Simple comparison:

```text
UserDetailsService
        ↓
"User ko load karo"

UserDetails
        ↓
"Ye loaded user ki security details hain"
```

---

# 28. UserDetailsService vs AuthenticationProvider

### UserDetailsService

```text
Load User
```

### AuthenticationProvider

```text
Authenticate User
```

Conceptually:

```text
AuthenticationProvider
        ↓
UserDetailsService
        ↓
UserDetails
        ↓
Password Verification
```

---

# 29. In-Memory UserDetailsService

Spring Security also provides:

```java
InMemoryUserDetailsManager
```

which implements `UserDetailsService`.

Flow:

```text
UserDetailsService
       ↑
       │
InMemoryUserDetailsManager
       ↓
Memory
```

---

# 30. Database UserDetailsService

Our implementation:

```text
UserDetailsService
       ↑
       │
CustomUserDetailsService
       ↓
UserRepository
       ↓
MySQL
```

Both approaches return:

```text
UserDetails
```

---

# 31. Real-World Architecture

Registration:

```text
POST /register
       ↓
User Data
       ↓
PasswordEncoder
       ↓
User Entity
       ↓
UserRepository
       ↓
MySQL
```

Login:

```text
POST /login
       ↓
Username + Password
       ↓
AuthenticationManager
       ↓
AuthenticationProvider
       ↓
UserDetailsService
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

# 32. Debugging UserDetailsService

Temporary debugging:

```java
System.out.println(
        "Username: [" + user.getUsername() + "]"
);

System.out.println(
        "Password: [" + user.getPassword() + "]"
);

System.out.println(
        "Role: [" + user.getRole() + "]"
);
```

Expected:

```text
Username: [vijay]
Password: [{noop}1234]
Role: [USER]
```

This helps verify that the database user is being loaded correctly.

Remove debug statements after testing.

---

# 33. Common Mistakes

### Mistake 1

Using:

```java
extends UserDetailsService
```

Incorrect because `UserDetailsService` is an interface.

Correct:

```java
implements UserDetailsService
```

---

### Mistake 2

Returning `User` Entity instead of `UserDetails`.

Incorrect conceptually:

```java
return user;
```

Correct:

```java
return new CustomUserDetails(user);
```

---

### Mistake 3

Forgetting authorities.

Without correct authorities:

```text
Authentication may succeed
        ↓
Authorization may fail
```

Example:

```text
ROLE_USER
```

cannot access:

```text
ROLE_ADMIN
```

---

### Mistake 4

Wrong role prefix.

When using:

```java
.hasRole("ADMIN")
```

the authority is normally:

```text
ROLE_ADMIN
```

---

### Mistake 5

User not found handling missing.

Use:

```java
throw new UsernameNotFoundException(...)
```

when no user is found.

---

# 34. Complete Example

## User Entity

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

---

## Repository

```java
public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
```

---

## CustomUserDetails

```java
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole()
                )
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

---

## CustomUserDetailsService

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

        return new CustomUserDetails(user);
    }
}
```

---

# 35. Important Interview Questions

## Q1. What is UserDetailsService?

`UserDetailsService` is a Spring Security interface used to load user details by username.

---

## Q2. What is the main method of UserDetailsService?

```java
loadUserByUsername(String username)
```

---

## Q3. What does `loadUserByUsername()` return?

It returns:

```java
UserDetails
```

---

## Q4. Does UserDetailsService authenticate the user?

Not by itself.

Its primary responsibility is to load user details. Authentication is handled through the authentication infrastructure, such as an `AuthenticationProvider`.

---

## Q5. What is UserDetails?

`UserDetails` represents security-related information about an authenticated user, including username, password, authorities, and account status.

---

## Q6. What is the difference between UserDetails and UserDetailsService?

```text
UserDetailsService
→ Loads the user

UserDetails
→ Represents the loaded user's security information
```

---

## Q7. Why implement UserDetailsService?

To provide Spring Security with custom logic for loading users from our application's data source.

---

## Q8. Why use CustomUserDetails?

It allows us to map our application-specific `User` entity into Spring Security's `UserDetails` model.

---

## Q9. What is GrantedAuthority?

It represents an authority granted to a user, such as:

```text
ROLE_USER
ROLE_ADMIN
USER_READ
```

---

## Q10. What is SimpleGrantedAuthority?

It is a simple implementation of `GrantedAuthority`.

Example:

```java
new SimpleGrantedAuthority("ROLE_ADMIN");
```

---

## Q11. What happens if the user is not found?

Typically, `UsernameNotFoundException` is thrown.

---

## Q12. Why is `getAuthorities()` important?

It provides the authorities used during authorization.

For example:

```text
ROLE_ADMIN
```

allows:

```java
.hasRole("ADMIN")
```

---

## Q13. What does `isEnabled()` represent?

It represents whether the user account is enabled.

```text
true  → enabled
false → disabled
```

---

## Q14. What does `isAccountNonLocked()` represent?

It represents whether the account is locked.

```text
true  → not locked
false → locked
```

---

## Q15. What does `isCredentialsNonExpired()` represent?

It represents whether the user's credentials are still valid and not expired.

---

# 36. Key Takeaways

```text
UserDetailsService
        ↓
Loads user
```

```text
loadUserByUsername()
        ↓
Find user by username
```

```text
UserDetails
        ↓
Represents security user information
```

```text
CustomUserDetails
        ↓
Maps application User → UserDetails
```

```text
GrantedAuthority
        ↓
Represents permissions/roles
```

```text
ROLE_ADMIN
        ↓
hasRole("ADMIN")
```

```text
ROLE_USER
        ↓
hasRole("USER")
```

---

# 37. Final Mental Model

```text
                    LOGIN
                      ↓
               Username + Password
                      ↓
             AuthenticationManager
                      ↓
            AuthenticationProvider
                      ↓
             UserDetailsService
                      ↓
          loadUserByUsername()
                      ↓
                UserRepository
                      ↓
                   MySQL
                      ↓
                User Entity
                      ↓
             CustomUserDetails
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
```

---

# 38. One-Line Revision

> `UserDetailsService` loads a user's security information by username, while `UserDetails` represents that loaded user's username, password, authorities, and account status for Spring Security.