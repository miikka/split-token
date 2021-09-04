# split-tokens

This library implements [split tokens].

[![Clojars Project](https://img.shields.io/clojars/v/net.clojars.miikka/split-token.svg)](https://clojars.org/net.clojars.miikka/split-token)

## What are split tokens?

A standard way to implement password reset tokens and similar is to [generate a random token][generate],
send it to your user, usually in a URL, and store it in your database.
When the user tries to use the token, you check that the token is in the database.

Split tokens improve upon this design. You still generate a random token and send it to your user,
but you store it in the database in two parts called _selector_ and _verifier_. Selector is stored
as-is and verifier is hashed before saving it.

When the user tries to use a token, you split it and use the selecotr to look up the verifier hash from the database.
Then you hash the user-supplied verifier and check that it matches the stored hash.
This achieves two things:

* It prevents [timing attacks]. Typically database lookup is suspectible to timing attacks, but a proper hash comparison is not.
* An attacker with read access to your token database cannot use the tokens themselves.

This basically the same as why you hash users' passwords with a password hashing function before storing them in the database,
except that we have a single randomly-generated token instead of a username and a password.

[generate]: https://quanttype.net/posts/2020-10-18-random-tokens-in-clojure.html
[split tokens]: https://paragonie.com/blog/2017/02/split-tokens-token-based-authentication-protocols-without-side-channels
[timing attacks]: https://soatok.blog/2021/08/20/lobste-rs-password-reset-vulnerability/

## Example

Generating a token:

```clojure
 (require '[split-token.core :as split-token])

(split-token/generate)
;; {:selector "gEHOHXOFanTHp43CbFWdCw",
;;  :verifier-hash "m0UYbYs2dhbeGHnsjCLY4w",
;;  :token "gEHOHXOFanTHp43CbFWdC0yKajTVYk58FpXoCt9FyQY"}
```

Validating a token:

```clojure
(let [token "gEHOHXOFanTHp43CbFWdC0yKajTVYk58FpXoCt9FyQY"]
  (split-token/get-selector token))
;; "gEHOHXOFanTHp43CbFWdCw"

;; At this point you'd look up the verifier hash from the database based on the selector.
;; Then you can verify it:

(let [token "gEHOHXOFanTHp43CbFWdC0yKajTVYk58FpXoCt9FyQY"
      verifier-hash "m0UYbYs2dhbeGHnsjCLY4w"]
  (split-token/valid? token verifier-hash))
;; true
```

## Technical details

* The library uses 32-byte tokens with 16-byte selectors and 16-byte verifiers.
* The hash is 128-bit BLAKE2b.
* To make things easy, all the functions return URL-safe Base64-encoded strings.
* There's no configuration. The implementation is one short file, though, so you could [vendor](https://stackoverflow.com/a/39643873) it.
* This library builds on [buddy-core](https://github.com/funcool/buddy-core), which builds on [Bouncy Castle](https://www.bouncycastle.org).

## Development

Run tests:

```sh
bin/kaocha

# Automatically run tests when files change
bin/kaocha --watch
```

Deployment:

```sh
export CLOJARS_USERNAME=...
export CLOJARS_PASSWORD=...

clj -T:build jar
clj -T:build deploy
```

## License

Copyright 2021 Miikka Koskinen. Distributed under the terms of ISC license, see `LICENSE`.
