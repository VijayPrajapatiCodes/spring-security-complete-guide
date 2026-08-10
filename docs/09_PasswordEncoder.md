# 🔐 09 — PasswordEncoder

## 1. What is PasswordEncoder?

`PasswordEncoder` is a Spring Security interface used to securely encode/hash passwords and verify passwords during authentication.

Basic flow:

```text
Raw Password
     ↓
PasswordEncoder
     ↓
Encoded/Hashed Password
     ↓
Database
```

Example:

```text
1234
 ↓
BCryptPasswordEncoder
 ↓
$2a$10$................
 ↓
MySQL
```

---

# 2. Why Do We Need PasswordEncoder?

We should never store user passwords directly in the database.

❌ Wrong:

```text
username = vijay
password = 1234
```

If the database is leaked:

```text
Database
   ↓
password = 1234
   ↓
Password exposed ❌
```

Instead:

```text
1234
 ↓
PasswordEncoder
 ↓
BCrypt Hash
 ↓
Database
```

Database stores the encoded password instead of the raw password.

---

# 3. Password Hashing vs Encryption

These concepts are different.

## Encryption

Encryption is generally reversible when the correct decryption key is available.

```text
Plain Text
    ↓
Encryption
    ↓
Encrypted Data
    ↓
Decryption
    ↓
Original Data
```

## Password Hashing

Password hashing is designed as a one-way process.

```text
Password
    ↓
Hashing
    ↓
Hash
```

During login, we don't normally decrypt the hash.

Instead, we verify the entered password against the stored hash.

---

# 4. PasswordEncoder Interface

Spring Security provides:

```java
public interface PasswordEncoder {

    String encode(CharSequence rawPassword);

    boolean matches(
            CharSequence rawPassword,
            String encodedPassword
    );
}
```

The two most important methods are:

```text
encode()
matches()
```

---

# 5. `encode()`

`encode()` converts a raw password into an encoded password.

Example:

```java
String encodedPassword =
        passwordEncoder.encode("1234");
```

Conceptually:

```text
1234
 ↓
BCrypt
 ↓
$2a$10$................
```

The generated result should be stored in the database.

---

# 6. `matches()`

`matches()` verifies a raw password against the stored encoded password.

Example:

```java
passwordEncoder.matches(
        "1234",
        storedPassword
);
```

If the password is correct:

```text
true
```

If the password is incorrect:

```text
false
```

Important:

```text
User enters password
        ↓
matches(rawPassword, storedHash)
        ↓
true / false
```

We do not decode the stored password.

---

# 7. BCryptPasswordEncoder

A commonly used Spring Security password encoder is:

```java
BCryptPasswordEncoder
```

Import:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
```

Example:

```java
BCryptPasswordEncoder encoder =
        new BCryptPasswordEncoder();
```

---

# 8. PasswordEncoder Bean

We register `PasswordEncoder` as a Spring Bean:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Imports:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
```

Complete:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Now Spring can inject:

```java
PasswordEncoder
```

wherever it is required.

---

# 9. Why Create a PasswordEncoder Bean?

Instead of creating:

```java
new BCryptPasswordEncoder()
```

everywhere, we create one Spring-managed bean.

```text
Spring Container
      ↓
PasswordEncoder Bean
      ↓
BCryptPasswordEncoder
```

Then we can inject it:

```java
private final PasswordEncoder passwordEncoder;
```

---

# 10. PasswordGenerator Practical

For learning/testing, we created:

```java
public class PasswordGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        System.out.println(
                encoder.encode("1234")
        );

        System.out.println(
                encoder.encode("123")
        );
    }
}
```

Output looks like:

```text
$2a$10$................
$2a$10$................
```

The exact values are different for every generated password.

---

# 11. Why Does BCrypt Generate Different Hashes?

Suppose:

```java
encoder.encode("1234");
```

is called multiple times.

The output can be different:

```text
$2a$10$ABC........
$2a$10$XYZ........
```

even though the original password is the same.

This is because password hashing uses a random salt.

---

# 12. What is Salt?

A salt is a random value used during password hashing.

Conceptually:

```text
Password
    +
Random Salt
    ↓
Hashing
    ↓
Encoded Password
```

This helps prevent attackers from efficiently using precomputed hash tables against many users.

---

# 13. PasswordEncoder and Database

Before PasswordEncoder:

```text
Database

vijay | {noop}1234 | USER
vp    | {noop}123  | ADMIN
```

After BCrypt:

```text
Database

vijay | $2a$10$........ | USER
vp    | $2a$10$........ | ADMIN
```

The database should contain the BCrypt encoded value, not the raw password.

---

# 14. `{noop}`

Earlier we used:

```text
{noop}1234
```

for learning.

`noop` means the password is being treated without hashing/encoding.

This is useful for simple Spring Security demonstrations but should not be used for real production passwords.

Production applications should use a suitable password hashing strategy such as BCrypt.

---

# 15. Registration Flow

When a new user registers:

```text
User enters:
12345
       ↓
Controller
       ↓
UserService
       ↓
PasswordEncoder.encode()
       ↓
BCrypt Hash
       ↓
User Entity
       ↓
UserRepository.save()
       ↓
MySQL
```

The raw password should not be stored.

---

# 16. UserService Practical

We created:

```java
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(User user) {

        String encodedPassword =
                passwordEncoder.encode(user.getPassword());

        user.setPassword(encodedPassword);

        return userRepository.save(user);
    }
}
```

---

# 17. Important Line

This is the most important line:

```java
String encodedPassword =
        passwordEncoder.encode(user.getPassword());
```

Suppose:

```text
user.getPassword()
        ↓
12345
```

Then:

```text
passwordEncoder.encode("12345")
        ↓
$2a$10$................
```

Then:

```java
user.setPassword(encodedPassword);
```

replaces the raw password with the encoded password before saving.

---

# 18. Registration Controller

We created:

```java
@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) {

        return userService.register(user);
    }
}
```

---

# 19. Registration Request

Request:

```http
POST http://localhost:8080/api/users/register
Content-Type: application/json
```

Body:

```json
{
    "username": "rahul",
    "password": "12345",
    "role": "USER"
}
```

Flow:

```text
POST /api/users/register
        ↓
UserController
        ↓
UserService
        ↓
PasswordEncoder
        ↓
BCrypt
        ↓
UserRepository
        ↓
MySQL
```

---

# 20. Database Result

Request contains:

```json
{
    "username": "rahul",
    "password": "12345",
    "role": "USER"
}
```

But database should contain something like:

```text
username | password             | role
---------|----------------------|------
rahul    | $2a$10$.............. | USER
```

The exact BCrypt hash will be different.

---

# 21. Login Flow

Registration uses:

```java
passwordEncoder.encode()
```

Login uses password verification.

Conceptually:

```text
User enters:
12345
       ↓
Authentication
       ↓
UserDetailsService
       ↓
Database
       ↓
Stored BCrypt Hash
       ↓
PasswordEncoder.matches()
       ↓
true / false
```

---

# 22. `encode()` vs `matches()`

This is extremely important.

### Registration

```java
passwordEncoder.encode(rawPassword);
```

Purpose:

```text
Raw Password
     ↓
Encoded Password
```

### Login

```java
passwordEncoder.matches(
    rawPassword,
    encodedPassword
);
```

Purpose:

```text
Entered Password
       +
Stored Hash
       ↓
Verification
       ↓
true / false
```

Remember:

```text
Registration → encode()
Login        → matches()
```

---

# 23. We Don't Decode BCrypt Passwords

Incorrect idea:

```text
BCrypt Hash
     ↓
Decode
     ↓
12345
```

❌ We don't normally do this.

Correct:

```text
User enters:
12345

       ↓

matches()

       ↓

Stored BCrypt Hash

       ↓

true / false
```

---

# 24. Complete Authentication Flow

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
UserRepository
  ↓
Database
  ↓
UserDetails
  ↓
PasswordEncoder
  ↓
matches()
  ↓
Authentication Success / Failure
```

---

# 25. PasswordEncoder with UserDetailsService

Our `CustomUserDetailsService` loads the user:

```java
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
```

The returned `UserDetails` contains:

```text
Username
Password Hash
Authorities
Account Status
```

Spring Security can then use the configured password encoder during authentication.

---

# 26. PasswordEncoder vs UserDetailsService

These have different responsibilities.

### UserDetailsService

```text
Load user from database
```

### PasswordEncoder

```text
Encode and verify passwords
```

Flow:

```text
UserDetailsService
        ↓
UserDetails
        ↓
PasswordEncoder
        ↓
Password Verification
```

---

# 27. PasswordEncoder vs Authentication

`PasswordEncoder` itself does not perform the entire login process.

It provides password encoding and matching functionality.

Authentication infrastructure uses it to verify credentials.

Conceptually:

```text
AuthenticationProvider
        ↓
UserDetailsService
        ↓
UserDetails
        ↓
PasswordEncoder
        ↓
Password Match
```

---

# 28. CSRF and Registration API

During our practical, the registration API initially redirected to the login page even though:

```java
.requestMatchers("/api/users/register")
.permitAll()
```

was configured.

The reason was CSRF protection.

---

# 29. `permitAll()` vs CSRF

These are different concepts.

### `permitAll()`

```java
.requestMatchers("/api/users/register")
.permitAll()
```

means:

> This endpoint does not require the user to be authenticated.

### CSRF

CSRF is a separate security protection mechanism.

Therefore:

```text
permitAll()
```

does not automatically mean:

```text
CSRF disabled
```

---

# 30. Why Did We Disable CSRF?

For our current learning REST API/Postman setup, we temporarily used:

```java
.csrf(csrf -> csrf.disable())
```

Example:

```java
@Bean
public SecurityFilterChain securityFilterChain(
        HttpSecurity http
) throws Exception {

    http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                    .requestMatchers("/hello")
                    .permitAll()

                    .requestMatchers("/api/users/register")
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

This was done for our current API-learning setup.

In a real production application, CSRF configuration should be decided based on the application's authentication architecture and whether browser cookies/session authentication are involved. It should not be disabled blindly.

---

# 31. Why Registration Needs `permitAll()`

A new user is not authenticated yet.

Therefore:

```text
New User
   ↓
/api/users/register
   ↓
Create Account
```

The endpoint should normally be publicly accessible.

Example:

```java
.requestMatchers("/api/users/register")
.permitAll()
```

---

# 32. BCrypt in Production

BCrypt is designed to be intentionally slower than fast general-purpose hashes.

This makes brute-force password guessing more expensive.

The work factor/cost can be configured:

```java
new BCryptPasswordEncoder(12);
```

For example:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

The appropriate cost should be selected based on the application's performance and security requirements rather than blindly copying a number.

---

# 33. Never Return Passwords in API Responses

Our learning controller currently returns the complete `User` entity:

```java
return userService.register(user);
```

If the entity contains a password field, the response can expose the BCrypt hash.

Even though the hash is not the raw password, it should still not be returned to clients.

Production applications should use a DTO.

Example:

```text
User Entity
     ↓
UserResponse DTO
     ↓
API Response
```

Instead of returning:

```json
{
    "username": "rahul",
    "password": "$2a$10$....",
    "role": "USER"
}
```

we should return something like:

```json
{
    "username": "rahul",
    "role": "USER"
}
```

---

# 34. Password Security Rules

Never:

```text
❌ Store raw passwords
❌ Return passwords in API responses
❌ Log raw passwords
❌ Send passwords to unnecessary services
❌ Use `{noop}` in production
```

Prefer:

```text
✅ PasswordEncoder
✅ BCrypt or another suitable password hashing strategy
✅ HTTPS
✅ Secure password policies
✅ Don't log passwords
✅ Don't expose password hashes in API responses
```

---

# 35. Common Mistakes

## Mistake 1 — Storing raw password

```text
1234
```

❌

Use:

```java
passwordEncoder.encode("1234");
```

---

## Mistake 2 — Encoding during login manually

Don't do:

```java
passwordEncoder.encode(loginPassword)
```

and compare the two encoded strings directly.

Because BCrypt uses a random salt, the encoded result can differ each time.

Use:

```java
passwordEncoder.matches(
        loginPassword,
        storedPassword
);
```

---

## Mistake 3 — Forgetting PasswordEncoder Bean

If Spring needs a `PasswordEncoder` and none is configured, authentication configuration can fail or use a different setup than intended.

Configure:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

---

## Mistake 4 — Keeping `{noop}` after switching to BCrypt

If you have moved to BCrypt, don't keep old raw/noop values for users you expect to authenticate with the BCrypt setup.

---

## Mistake 5 — Thinking BCrypt can be decrypted

BCrypt is a password hashing algorithm, not something you normally decrypt to recover the original password.

---

# 36. Complete PasswordEncoder Architecture

```text
                 REGISTRATION

Raw Password
     ↓
PasswordEncoder.encode()
     ↓
BCrypt
     ↓
Password Hash
     ↓
User Entity
     ↓
UserRepository
     ↓
MySQL
```

Login:

```text
                   LOGIN

Username + Password
        ↓
AuthenticationManager
        ↓
AuthenticationProvider
        ↓
UserDetailsService
        ↓
UserRepository
        ↓
UserDetails
        ↓
Stored Password Hash
        ↓
PasswordEncoder.matches()
        ↓
   ┌──────────────┐
   │              │
 true           false
   ↓              ↓
Success         Failure
```

---

# 37. Important Code

## PasswordEncoder Bean

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

## Encoding

```java
String encodedPassword =
        passwordEncoder.encode(user.getPassword());

user.setPassword(encodedPassword);
```

## Verification

```java
passwordEncoder.matches(
        rawPassword,
        encodedPassword
);
```

---

# 38. Interview Questions

## Q1. What is PasswordEncoder?

`PasswordEncoder` is a Spring Security interface used to encode passwords and verify raw passwords against stored encoded passwords.

---

## Q2. Why should passwords not be stored as plain text?

Because a database leak would directly expose users' passwords.

---

## Q3. What is BCryptPasswordEncoder?

It is a Spring Security implementation that uses BCrypt password hashing.

---

## Q4. What are the main methods of PasswordEncoder?

```java
encode()
matches()
```

---

## Q5. What is the difference between `encode()` and `matches()`?

```text
encode()
→ Converts raw password into an encoded password.

matches()
→ Verifies a raw password against a stored encoded password.
```

---

## Q6. Can we decrypt a BCrypt password?

No. BCrypt is designed as a one-way password hashing algorithm.

---

## Q7. Why does BCrypt produce different hashes for the same password?

Because a random salt is used during password hashing.

---

## Q8. Where should password encoding happen during registration?

Before saving the user to the database.

```text
Raw Password
    ↓
PasswordEncoder.encode()
    ↓
UserRepository.save()
```

---

## Q9. What should happen during login?

The entered password should be verified against the stored encoded password.

```text
Raw Password
    ↓
PasswordEncoder.matches()
    ↓
Stored Hash
```

---

## Q10. Why should we not compare two BCrypt encoded strings directly?

Because BCrypt uses a random salt, so encoding the same password again can produce a different hash.

Use:

```java
passwordEncoder.matches(
    rawPassword,
    storedHash
);
```

---

## Q11. What is `{noop}`?

`{noop}` indicates that a password is being handled without password hashing/encoding. It is useful for simple demonstrations but is not appropriate for production password storage.

---

## Q12. What is the difference between `UserDetailsService` and `PasswordEncoder`?

```text
UserDetailsService
→ Loads user details.

PasswordEncoder
→ Encodes and verifies passwords.
```

---

## Q13. Why do we create PasswordEncoder as a Spring Bean?

So Spring can manage the encoder and inject it wherever password encoding/verification is required.

---

## Q14. Should a REST API return the password hash?

No. Password information, including the stored hash, should not be exposed in API responses.

Use DTOs to control response data.

---

# 39. Key Takeaways

```text
PasswordEncoder
      ↓
Password Security
```

```text
Registration
      ↓
encode()
```

```text
Login
      ↓
matches()
```

```text
BCrypt
      ↓
Salt + Adaptive Password Hashing
```

```text
Database
      ↓
Store BCrypt Hash
```

Never:

```text
Raw Password → Database ❌
```

Always:

```text
Raw Password
      ↓
PasswordEncoder
      ↓
Hash
      ↓
Database
```

---

# 40. One-Line Revision

> `PasswordEncoder` securely encodes passwords during registration and verifies entered passwords against stored encoded passwords during authentication.

---

# 41. Final Mental Model

```text
                    USER REGISTRATION
                           ↓
                    Raw Password
                           ↓
                  PasswordEncoder
                           ↓
                 BCryptPasswordEncoder
                           ↓
                    BCrypt Hash
                           ↓
                     MySQL


                       USER LOGIN
                           ↓
                 Username + Password
                           ↓
                UserDetailsService
                           ↓
                     MySQL User
                           ↓
                     UserDetails
                           ↓
                 Stored BCrypt Hash
                           ↓
                PasswordEncoder.matches()
                           ↓
                    Authentication
                           ↓
                  Success / Failure
```