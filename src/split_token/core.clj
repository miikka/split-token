;; Copyright 2021 Miikka Koskinen
;;
;; Permission to use, copy, modify, and/or distribute this software for any
;; purpose with or without fee is hereby granted, provided that the above
;; copyright notice and this permission notice appear in all copies.
;;
;; THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
;; REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
;; FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
;; INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
;; LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
;; OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
;; PERFORMANCE OF THIS SOFTWARE.

(ns split-token.core
  (:require [buddy.core.bytes :as bytes]
            [buddy.core.codecs :as codecs]
            [buddy.core.hash :as hash]
            [buddy.core.nonce :as nonce]))

(defn- bytes->b64u-str [data]
  (codecs/bytes->str (codecs/bytes->b64u data)))

(defn- b64u-str->bytes [data]
  (codecs/b64u->bytes (codecs/to-bytes data)))

(defn generate
  "Generate a split token."
  []
  (let [selector (nonce/random-bytes 16)
        verifier (nonce/random-bytes 16)
        verifier-hash (hash/blake2b-128 verifier)
        token (bytes/concat selector verifier)]
    {:selector (bytes->b64u-str selector)
     :verifier-hash (bytes->b64u-str verifier-hash)
     :token (bytes->b64u-str token)}))

(defn get-selector
  "Extract the selector from a split token."
  [token]
  (bytes->b64u-str (bytes/slice (b64u-str->bytes token) 0 16)))

(defn valid?
  "Verify a split token. Returns true if the token matches the verifier hash."
  [token verifier-hash]
  (let [verifier (bytes/slice (b64u-str->bytes token) 16 32)]
    (bytes/equals? (hash/blake2b-128 verifier) (b64u-str->bytes verifier-hash))))

