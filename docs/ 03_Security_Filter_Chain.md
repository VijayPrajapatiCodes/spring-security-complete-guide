1. Filter              → Request ko intercept/process karta hai.
2. Request Filter me   → Tomcat/Servlet pipeline ke through aati hai.
3. Filter process      → Request ko inspect/modify/allow/block kar sakta hai.
4. doFilter()          → Request ko next filter/step par bhejta hai.
5. Filter position     → Before, After, At se define kar sakte hain.
6. addFilterBefore()   → Custom filter ko specified filter se pehle lagata hai.


# 🔐 03 — Security Filter Chain

## 1. What is a Filter?

A Filter is a component that can intercept and process an HTTP request before it reaches the Controller.

Basic flow:

```text
Client
   ↓
HTTP Request
   ↓
Filter
   ↓
Controller
```

A Filter can:

- Inspect a request
- Read request headers
- Read request parameters
- Perform authentication-related processing
- Perform authorization-related processing
- Modify request/response
- Allow a request to continue
- Block a request

---

# 2. Why Does Spring Security Use Filters?

Spring Security needs to process security before a request reaches the Controller.

Instead of writing security logic inside every Controller:

```java
@GetMapping("/admin")
public String admin() {

    // Check authentication
    // Check authorization

    return "Admin Data";
}
```

Spring Security processes requests before they reach the Controller.

Basic flow:

```text
Request
   ↓
Security Filters
   ↓
Authentication
   ↓
Authorization
   ↓
Controller
```

This keeps security logic centralized.

---

# 3. What is a Filter Chain?

When multiple filters are executed one after another, they form a Filter Chain.

Example:

```text
Request
   ↓
Filter 1
   ↓
Filter 2
   ↓
Filter 3
   ↓
Filter 4
   ↓
Controller
```

Each filter performs its own processing and can pass the request to the next filter.

---

# 4. Spring Security Filter Chain

Spring Security uses a chain of security filters to process incoming HTTP requests.

Basic flow:

```text
HTTP Request
      ↓
Security Filter 1
      ↓
Security Filter 2
      ↓
Security Filter 3
      ↓
Security Filter 4
      ↓
...
      ↓
Controller
```

The exact filters in the chain depend on the application's security configuration.

---

# 5. SecurityFilterChain

`SecurityFilterChain` is one of the most important components in modern Spring Security.

It defines the security filters and security rules that apply to matching HTTP requests.

Our configuration:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/hello").permitAll()
            .anyRequest().authenticated()
        );

    return http.build();
}
```

Meaning:

```text
/hello
   ↓
permitAll()
   ↓
Public

Other Requests
   ↓
authenticated()
   ↓
Authentication Required
```

---

# 6. Our SecurityConfig

Our current `SecurityConfig.java` looks like:

```java
package com.vijay.spring_security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/hello").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
```

Important parts:

```java
@Configuration
```

Tells Spring that this class contains configuration.

```java
@Bean
```

Registers the `SecurityFilterChain` as a Spring Bean.

```java
SecurityFilterChain
```

Defines our HTTP security configuration.

```java
HttpSecurity
```

Provides methods to configure HTTP security.

```java
.requestMatchers("/hello").permitAll()
```

Allows everyone to access `/hello`.

```java
.anyRequest().authenticated()
```

Requires authentication for all other requests.

```java
return http.build();
```

Builds and returns the configured SecurityFilterChain.

---

# 7. How a Request Reaches the Security Filter Chain

Simplified Spring Security architecture:

```text
Client
   ↓
HTTP Request
   ↓
Tomcat / Servlet Container
   ↓
DelegatingFilterProxy
   ↓
FilterChainProxy
   ↓
SecurityFilterChain
   ↓
Security Filters
   ↓
DispatcherServlet
   ↓
Controller
```

---

# 8. Servlet Container

Spring Boot web applications normally run inside a Servlet Container.

Common Servlet Containers:

- Tomcat
- Jetty
- Undertow

Spring Boot commonly uses embedded Tomcat by default.

The Servlet Container receives HTTP requests and manages the Servlet/Filter processing pipeline.

Example:

```text
Client
   ↓
HTTP Request
   ↓
Tomcat
```

---

# 9. DelegatingFilterProxy

`DelegatingFilterProxy` acts as a bridge between the Servlet Container and Spring-managed components.

Conceptually:

```text
Servlet Container
       ↓
DelegatingFilterProxy
       ↓
Spring Security
```

The Servlet Container works with Servlet Filters, while Spring Security manages its components inside the Spring ApplicationContext.

In modern Spring Boot applications, this infrastructure is normally configured automatically.

---

# 10. FilterChainProxy

`FilterChainProxy` is a central Spring Security filter.

Its responsibility is to delegate requests to the appropriate `SecurityFilterChain`.

Flow:

```text
DelegatingFilterProxy
       ↓
FilterChainProxy
       ↓
SecurityFilterChain
       ↓
Security Filters
```

If multiple security filter chains are configured, the appropriate chain can be selected based on the request.

---

# 11. Security Filters

A `SecurityFilterChain` contains multiple security filters.

Conceptually:

```text
SecurityFilterChain
       │
       ├── Security Filter 1
       ├── Security Filter 2
       ├── Security Filter 3
       ├── Security Filter 4
       └── Security Filter N
```

Different filters have different responsibilities.

Examples include filters related to:

- Authentication
- Authorization
- CSRF
- Session Management
- Exception Handling
- JWT / Bearer Token Authentication

The actual filters depend on the application's configuration.

---

# 12. How Does a Filter Process a Request?

A filter receives:

```text
HttpServletRequest
HttpServletResponse
FilterChain
```

Example:

```java
@Override
protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

    System.out.println(request.getRequestURI());

    filterChain.doFilter(request, response);
}
```

Here the filter:

1. Receives the request
2. Reads the request URI
3. Prints the URI
4. Passes the request to the next filter

---

# 13. What is `filterChain.doFilter()`?

This is one of the most important concepts.

```java
filterChain.doFilter(request, response);
```

Meaning:

> Continue processing the request through the remaining filter chain.

Example:

```text
MyFilter
   ↓
filterChain.doFilter()
   ↓
Next Filter
   ↓
Next Filter
   ↓
Controller
```

In simple words:

> "Mera filter ka kaam complete ho gaya, ab request ko next filter ke paas bhejo."

---

# 14. What Happens If `doFilter()` Is Not Called?

If a filter does not call:

```java
filterChain.doFilter(request, response);
```

the request normally does not continue to the next filter.

Flow:

```text
Request
   ↓
MyFilter
   ↓
STOP
```

However, the filter can intentionally complete the response itself.

Example:

```java
response.setStatus(HttpServletResponse.SC_FORBIDDEN);
return;
```

Flow:

```text
Request
   ↓
MyFilter
   ↓
Access Denied
   ↓
403 Forbidden
```

The Controller does not receive the request.

---

# 15. Filter Can Block a Request

A Filter can decide whether a request should continue.

Example:

```java
if (someCondition) {

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    return;
}

filterChain.doFilter(request, response);
```

Flow:

```text
Request
   ↓
Filter
   ↓
Condition Check
   │
   ├── Allowed
   │      ↓
   │   Next Filter
   │
   └── Not Allowed
          ↓
        STOP
```

This concept is important for security.

---

# 16. Our `MyFilter`

For learning, we created a custom filter:

```text
config/
│
├── SecurityConfig.java
└── MyFilter.java
```

Our `MyFilter.java`:

```java
package com.vijay.spring_security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class MyFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println(
                "🔥 MyFilter: " + request.getRequestURI()
        );

        filterChain.doFilter(request, response);
    }
}
```

---

# 17. What Does `MyFilter` Actually Do?

Important:

Our `MyFilter` is NOT performing authentication or authorization.

It only:

```text
✅ Intercepts the request
✅ Reads the request URI
✅ Prints the URI
✅ Passes the request forward
```

It is NOT:

```text
❌ Checking username
❌ Checking password
❌ Validating JWT
❌ Authenticating the user
❌ Checking roles
❌ Performing authorization
```

The purpose of `MyFilter` is to understand how a Filter works.

---

# 18. How Does MyFilter Work?

Suppose the browser sends:

```http
GET /hello
```

The request reaches `MyFilter`.

This code:

```java
request.getRequestURI()
```

returns:

```text
/hello
```

Then:

```java
System.out.println(
        "🔥 MyFilter: " + request.getRequestURI()
);
```

prints:

```text
🔥 MyFilter: /hello
```

Then:

```java
filterChain.doFilter(request, response);
```

passes the request to the next filter.

Complete flow:

```text
Browser
   ↓
GET /hello
   ↓
MyFilter
   │
   ├── Read /hello
   ├── Print /hello
   │
   ↓
Next Security Filter
   ↓
Controller
```

---

# 19. Registering MyFilter in SecurityFilterChain

A custom filter needs to be added to the SecurityFilterChain.

We used:

```java
.addFilterBefore(
        new MyFilter(),
        UsernamePasswordAuthenticationFilter.class
)
```

Complete configuration:

```java
package com.vijay.spring_security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

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
                );

        return http.build();
    }
}
```

---

# 20. What is `addFilterBefore()`?

`addFilterBefore()` places a custom filter before a specified filter in the SecurityFilterChain.

Example:

```java
http.addFilterBefore(
        new MyFilter(),
        UsernamePasswordAuthenticationFilter.class
);
```

Meaning:

```text
MyFilter
   ↓
UsernamePasswordAuthenticationFilter
```

So our `MyFilter` executes before the specified authentication filter.

---

# 21. What is UsernamePasswordAuthenticationFilter?

`UsernamePasswordAuthenticationFilter` is a Spring Security filter involved in username/password authentication processing.

Conceptually:

```text
Username
+
Password
      ↓
UsernamePasswordAuthenticationFilter
      ↓
Authentication Processing
```

Its exact behavior depends on the application's authentication configuration.

We use it here as a reference point for positioning our custom filter.

---

# 22. Why Did We Put MyFilter Before UsernamePasswordAuthenticationFilter?

We used:

```java
.addFilterBefore(
        new MyFilter(),
        UsernamePasswordAuthenticationFilter.class
)
```

This means:

```text
HTTP Request
      ↓
MyFilter
      ↓
UsernamePasswordAuthenticationFilter
      ↓
Other Security Filters
      ↓
Authorization
      ↓
Controller
```

The main concept is:

> `MyFilter` is placed before the specified filter.

---

# 23. `addFilterAfter()`

Spring Security also provides:

```java
addFilterAfter()
```

Example:

```java
http.addFilterAfter(
        new MyFilter(),
        UsernamePasswordAuthenticationFilter.class
);
```

Meaning:

```text
UsernamePasswordAuthenticationFilter
   ↓
MyFilter
```

So:

```text
addFilterBefore()
    ↓
Custom Filter comes before specified filter

addFilterAfter()
    ↓
Custom Filter comes after specified filter
```

---

# 24. `addFilterAt()`

Another method is:

```java
addFilterAt()
```

Example:

```java
http.addFilterAt(
        new MyFilter(),
        SomeFilter.class
);
```

It can place a filter at the position associated with the specified filter.

Filter placement should be done carefully because filter ordering can affect security behavior.

---

# 25. Why Is Filter Order Important?

Filters execute in a specific order.

Example:

```text
Request
   ↓
Filter A
   ↓
Filter B
   ↓
Filter C
   ↓
Controller
```

Suppose Filter B depends on something done by Filter A.

Then changing the order can change the behavior:

```text
Filter B
   ↓
Filter A
```

Therefore, filter order is important in Spring Security.

---

# 26. OncePerRequestFilter

Our `MyFilter` extends:

```java
OncePerRequestFilter
```

Example:

```java
public class MyFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Filter logic

        filterChain.doFilter(request, response);
    }
}
```

`OncePerRequestFilter` is a Spring-provided base class commonly used for custom request filters.

It is especially common when creating JWT authentication filters.

---

# 27. Filter vs FilterChain vs SecurityFilterChain

## Filter

A Filter is a component that processes/intercepts a request.

```text
Request
   ↓
Filter
```

## Filter Chain

A Filter Chain is a sequence of filters.

```text
Request
   ↓
Filter
   ↓
Filter
   ↓
Filter
```

## SecurityFilterChain

`SecurityFilterChain` is Spring Security's configured security filter chain.

```text
Request
   ↓
SecurityFilterChain
   ↓
Security Filters
   ↓
Authentication
   ↓
Authorization
```

Remember:

```text
Filter
    ↓
Single processing component

FilterChain
    ↓
Sequence of filters

SecurityFilterChain
    ↓
Spring Security security filter chain
```

---

# 28. Current Project Flow

Our current project flow is approximately:

```text
Client
   ↓
HTTP Request
   ↓
Tomcat
   ↓
DelegatingFilterProxy
   ↓
FilterChainProxy
   ↓
SecurityFilterChain
   ↓
MyFilter
   ↓
Other Security Filters
   ↓
Authorization
   ↓
DispatcherServlet
   ↓
Controller
```

---

# 29. `/hello` Request

Our security rule:

```java
.requestMatchers("/hello").permitAll()
```

When we request:

```text
GET /hello
```

Flow:

```text
Browser
   ↓
GET /hello
   ↓
MyFilter
   ↓
Print "/hello"
   ↓
Security Processing
   ↓
permitAll()
   ↓
DispatcherServlet
   ↓
TestController
   ↓
Hello Spring Security
```

Authentication is not required for `/hello`.

---

# 30. `/user` Request

Our security rule:

```java
.anyRequest().authenticated()
```

When we request:

```text
GET /user
```

Flow:

```text
Browser
   ↓
GET /user
   ↓
MyFilter
   ↓
Security Processing
   ↓
Authentication Required
   ↓
Login
   ↓
Authentication
   ↓
Authorization
   ↓
DispatcherServlet
   ↓
TestController
```

---

# 31. JWT Filter Concept

Later we will create:

```text
JwtAuthenticationFilter
```

Suppose the client sends:

```http
GET /api/orders
Authorization: Bearer eyJhbGciOi...
```

A JWT filter can process the request:

```text
Request
   ↓
JwtAuthenticationFilter
   ↓
Read Authorization Header
   ↓
Extract JWT
   ↓
Validate JWT
   ↓
Identify User
   ↓
Create Authentication
   ↓
Set SecurityContext
   ↓
Continue Filter Chain
   ↓
Authorization
   ↓
Controller
```

A common configuration pattern is:

```java
http.addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
);
```

JWT implementation will be covered later.

---

# 32. Complete Spring Security Filter Flow

```text
                         CLIENT
                           │
                           ▼
                     HTTP REQUEST
                           │
                           ▼
                         TOMCAT
                           │
                           ▼
                DelegatingFilterProxy
                           │
                           ▼
                   FilterChainProxy
                           │
                           ▼
                SecurityFilterChain
                           │
                           ▼
                    Security Filters
                           │
                    ┌──────┴──────┐
                    ▼             ▼
              Authentication  Authorization
                    │             │
                    └──────┬──────┘
                           ▼
                  DispatcherServlet
                           │
                           ▼
                      Controller
                           │
                           ▼
                        Service
                           │
                           ▼
                      Repository
                           │
                           ▼
                       Database
```

---

# 33. Six Most Important Questions

## Q1. What is a Filter?

A Filter is a component that intercepts and processes an HTTP request before it reaches the Controller.

---

## Q2. How does a request reach a Filter?

The HTTP request enters the Servlet Container such as Tomcat and passes through the configured filter chain.

Simplified:

```text
Client
   ↓
Tomcat
   ↓
Filter
   ↓
Controller
```

Spring Security:

```text
Client
   ↓
Tomcat
   ↓
DelegatingFilterProxy
   ↓
FilterChainProxy
   ↓
SecurityFilterChain
   ↓
Security Filters
```

---

## Q3. How does a Filter process a request?

A Filter receives the request and response and can inspect, modify, allow, or block the request.

Example:

```java
System.out.println(request.getRequestURI());

filterChain.doFilter(request, response);
```

---

## Q4. What does `filterChain.doFilter()` do?

It passes the request and response to the next filter in the chain.

```text
Current Filter
      ↓
filterChain.doFilter()
      ↓
Next Filter
```

---

## Q5. How do we position a Filter in the Filter Chain?

Spring Security provides:

```java
addFilterBefore()
addFilterAfter()
addFilterAt()
```

---

## Q6. What does `addFilterBefore()` do?

It places a custom filter before a specified filter.

Example:

```java
http.addFilterBefore(
        new MyFilter(),
        UsernamePasswordAuthenticationFilter.class
);
```

Flow:

```text
MyFilter
   ↓
UsernamePasswordAuthenticationFilter
```

---

# 34. Interview Questions

### Q1. What is a Filter?

A Filter is a component that can intercept and process an HTTP request before it reaches the target Servlet or Controller.

### Q2. What is a Filter Chain?

A Filter Chain is a sequence of filters through which a request passes.

### Q3. What is SecurityFilterChain?

`SecurityFilterChain` defines the security filters and security configuration applied to matching HTTP requests.

### Q4. What does `filterChain.doFilter()` do?

It passes the request and response to the next filter in the chain.

### Q5. What happens if a filter does not call `doFilter()`?

The request does not continue to the next filter unless the filter completes the response itself.

### Q6. What is `addFilterBefore()`?

It places a custom filter before a specified filter in the `SecurityFilterChain`.

### Q7. What is `addFilterAfter()`?

It places a custom filter after a specified filter in the `SecurityFilterChain`.

### Q8. What is `OncePerRequestFilter`?

It is a Spring-provided base class commonly used to create custom request filters intended to execute once per request dispatch.

### Q9. Why are Filters important in Spring Security?

Filters allow Spring Security to process authentication, authorization, JWT, sessions, CSRF, and other security concerns before requests reach the Controller.

### Q10. Why is Filter order important?

Because one security filter can depend on processing performed by another filter, so the order can affect authentication and authorization behavior.

---

# 35. Key Takeaways

### Filter

```text
Intercepts and processes HTTP requests
```

### Filter Chain

```text
Sequence of filters
```

### SecurityFilterChain

```text
Spring Security's configured security filter chain
```

### filterChain.doFilter()

```text
Continue the request to the next filter
```

### addFilterBefore()

```text
Place a custom filter before another filter
```

### addFilterAfter()

```text
Place a custom filter after another filter
```

### OncePerRequestFilter

```text
Convenient base class for custom request filters
```

---

# 36. Final Mental Model

Always remember:

```text
HTTP Request
      ↓
SecurityFilterChain
      ↓
Security Filters
      ↓
Authentication
      ↓
Authorization
      ↓
Controller
```

For our custom filter:

```text
Request
   ↓
MyFilter
   ↓
Inspect / Process Request
   ↓
filterChain.doFilter()
   ↓
Next Filter
   ↓
Controller
```

For JWT:

```text
Request
   ↓
JwtAuthenticationFilter
   ↓
Extract JWT
   ↓
Validate JWT
   ↓
Create Authentication
   ↓
SecurityContext
   ↓
Next Filter
   ↓
Authorization
   ↓
Controller
```

---

# 37. One-Line Revision

> SecurityFilterChain is a chain of security filters through which incoming HTTP requests are processed before reaching the Controller.

> Filter = Request ko intercept/process karta hai.

> filterChain.doFilter() = Request ko next filter par bhejta hai.

> addFilterBefore() = Custom filter ko specified filter se pehle place karta hai.

> MyFilter = Hamare practical me request URI print karke request ko next filter par forward karta hai.