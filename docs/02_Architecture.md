# 🔐 Spring Security — Architecture

## 1. Introduction

Spring Security incoming HTTP requests ko security filters ke through process karta hai.

Basic flow:

Client
↓
HTTP Request
↓
Servlet Container (Tomcat)
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
↓
Service
↓
Repository
↓
Database


# 2. Servlet Container

Spring Boot web application ek Servlet Container ke andar run hoti hai.

Common Servlet Containers:

- Tomcat
- Jetty
- Undertow

Spring Boot ka default embedded server generally Tomcat hota hai.

Servlet Container ka main kaam:

- HTTP requests receive karna
- HTTP responses send karna
- Servlet lifecycle manage karna
- Filters ko execute karna

Example:

GET /hello

Client
↓
Tomcat


# 3. Servlet Filter

Servlet Filter ek component hai jo request ko servlet/controller tak pahunchne se pehle process kar sakta hai.

Basic flow:

Request
↓
Filter
↓
Servlet

Filter ka use:

- Request logging
- Authentication-related processing
- Authorization-related processing
- Request modification
- Response modification
- Security checks

Spring Security ka architecture heavily filters par based hai.


# 4. Spring Security Filters

Spring Security incoming requests ko security filters ke through process karta hai.

Basic flow:

Client
↓
HTTP Request
↓
Security Filters
↓
Controller

Security filters different security-related tasks perform kar sakte hain.

Examples:

- Authentication
- Authorization
- CSRF protection
- Session management
- Exception handling
- JWT processing

Exact filters application ki configuration par depend karte hain.


# 5. DelegatingFilterProxy

`DelegatingFilterProxy` Servlet Container aur Spring ApplicationContext ke beech bridge ka kaam karta hai.

Conceptually:

Servlet Container
↓
DelegatingFilterProxy
↓
Spring Security

Servlet Container filters ko manage karta hai, jabki Spring Security ke components Spring ApplicationContext me managed beans hote hain.

`DelegatingFilterProxy` in dono systems ko connect karta hai.

Modern Spring Boot applications me ise manually configure karne ki normally zarurat nahi hoti.


# 6. FilterChainProxy

`FilterChainProxy` Spring Security ka central filter hai.

Iska main kaam request ko appropriate `SecurityFilterChain` tak pahunchana hai.

Flow:

DelegatingFilterProxy
↓
FilterChainProxy
↓
SecurityFilterChain

Agar application me multiple security filter chains configured hain, to `FilterChainProxy` request ke according appropriate chain select kar sakta hai.


# 7. SecurityFilterChain

`SecurityFilterChain` Spring Security ka one of the most important components hai.

Ye define karta hai ki incoming HTTP requests ke liye kaunse security filters aur security rules apply honge.

Example:

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