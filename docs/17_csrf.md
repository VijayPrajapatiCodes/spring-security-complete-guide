# 10 — CSRF (Cross-Site Request Forgery)

## 1. CSRF kya hai?

CSRF ka full form hai:

> Cross-Site Request Forgery

CSRF ek web security attack hai jisme attacker victim ke browser ka use karke kisi trusted website par unwanted/forged request perform karwane ki koshish karta hai.

Simple words mein:

> User already kisi website par logged in hai aur attacker user ke browser se us website par malicious request bhejne ki koshish karta hai.

---

# 2. CSRF Attack ko Samjho

Maan lo user kisi banking website par logged in hai:

```text
https://bank.example.com