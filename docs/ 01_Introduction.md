# 🔐 Spring Security — Introduction

## 1. What is Spring Security?

Spring Security is a powerful security framework for Spring applications.

It is mainly used for:

- Authentication
- Authorization
- Password Security
- Session Management
- Protection against common web attacks
- JWT-based Authentication
- OAuth2
- Social Login

In simple words:

Spring Security helps the application decide:

1. Who is the user?
2. Is the user authenticated?
3. What is the user allowed to access?

---

# 2. Why Do We Need Spring Security?

Suppose we have a REST API:

```text
Client
   ↓
Controller
   ↓
Service
   ↓
Database