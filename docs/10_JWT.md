# JWT Authentication

## 1. JWT kya hai?

JWT ka full form **JSON Web Token** hai.

JWT ek token format hai jiska use authentication ke liye kiya jata hai.

JWT ke through server authenticated user ko identify kar sakta hai bina traditional server-side session ke.

---

## 2. JWT Authentication Flow

```text
Client
   ↓
Login
   ↓
Username + Password
   ↓
AuthenticationManager
   ↓
UserDetailsService
   ↓
Database
   ↓
Password Verification
   ↓
Authentication Success
   ↓
JWT Generate
   ↓
Access Token
```

---

## 3. JWT Structure

JWT ke 3 parts hote hain:

```text
Header.Payload.Signature
```

### Header

Header me algorithm aur token type hota hai.

Example:

```json
{
  "alg": "HS384",
  "typ": "JWT"
}
```

### Payload

Payload me claims hote hain.

Example:

```json
{
  "sub": "vijay",
  "iat": 1234567890,
  "exp": 1234569999
}
```

### Signature

Signature token ki integrity verify karne ke liye use hoti hai.

---

## 4. Access Token

Access Token ka use protected APIs ko access karne ke liye hota hai.

Example:

```http
Authorization: Bearer <ACCESS_TOKEN>
```

---

## 5. JwtAuthenticationFilter

JWT ko request se read karne ke liye custom filter banaya jata hai.

```java
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authHeader.substring(7);

        String username =
                jwtService.extractUsername(token);

        UserDetails userDetails =
                userDetailsService
                        .loadUserByUsername(username);

        if (jwtService.validateToken(token, username)) {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
```

---

## 6. JWT Request Flow

```text
HTTP Request
     ↓
Authorization Header
     ↓
Bearer Token
     ↓
JwtAuthenticationFilter
     ↓
Extract Username
     ↓
Load UserDetails
     ↓
Validate JWT
     ↓
Create Authentication
     ↓
SecurityContextHolder
     ↓
Controller
```

---

## 7. SecurityContextHolder

Successful authentication ke baad:

```java
SecurityContextHolder
        .getContext()
        .setAuthentication(authentication);
```

Spring Security current authenticated user ko SecurityContext me store karta hai.

---

## 8. Stateless Authentication

JWT authentication generally stateless hoti hai.

Server ko traditional HTTP session maintain karne ki zarurat nahi hoti.

```text
Client
  ↓
JWT
  ↓
Server
  ↓
Validate
```

---

## 9. JWT Advantages

- Stateless authentication
- REST APIs ke liye useful
- Scalable
- Web aur mobile applications ke saath useful
- Session dependency kam

---

## 10. JWT Disadvantages

- Token leak hone par security risk
- Token revoke karna difficult ho sakta hai
- Secret key secure rakhni hoti hai
- JWT payload unnecessarily large nahi hona chahiye

---

## 11. Important Interview Questions

### Q1. JWT kya hai?

JWT ek token format hai jo authentication aur information exchange ke liye use hota hai.

### Q2. JWT ke kitne parts hote hain?

```text
Header
Payload
Signature
```

### Q3. JWT request me kaise bhejte hain?

```http
Authorization: Bearer <TOKEN>
```

### Q4. JwtAuthenticationFilter ka kya use hai?

Request se JWT read karke token validate karta hai aur authenticated user ko SecurityContext me set karta hai.

### Q5. SecurityContextHolder kya hai?

Current authenticated user ki authentication information ko hold karta hai.